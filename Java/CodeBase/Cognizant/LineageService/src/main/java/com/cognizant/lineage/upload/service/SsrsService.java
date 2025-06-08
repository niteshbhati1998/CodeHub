package com.cognizant.lineage.upload.service;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

@Service
public class SsrsService {

    @Value("${csrfToken}")
    private String csrfToken;

    @Value("${logFileLocation}")
    private String logFileDirectory;

    @Value("${ssrsUploadLocation}")
    String ssrsUploadLocation;

    @Value("${ssrsInputLocation}")
    String ssrsInputLocation;

    @Value("${ssrsOutputLocation}")
    String ssrsOutputLocation;

    @Value("${bteqScriptName}")
    private String lineageShellScript;

    @Value("${bteqScriptLocation}")
    private String lineageShellScriptLocation;

    @Value("${commonScriptName}")
    private String graphDataLoadScript;

    @Value("${commonScriptLocation}")
    private String graphDataLoadScriptLocation;

    @Autowired
    LineageJobRepository jobRepo;

    @Autowired
    LineageJobStatusRepository lineageJobStatusRepository;

    @Autowired
    ScriptLineageService scriptLineageService;

    @Autowired
    ScriptComplexity scriptComplexity;

    public LineageJob uploadAndParseSsrsScripts(MultipartFile[] files, String projectName, String technology,
                                                Logger LOGGER) throws LineageBusinessException {
        try {

            Long jobId = jobRepo.getJobIdByMax();

            LineageJob lineageJob = new LineageJob();
            lineageJob.setJobId(jobId);
            lineageJob.setProjectName(projectName);
            lineageJob.setTechnology(TechnologyConstants.SSRS);
            lineageJob.setStartTime(new Date());
            lineageJob.setJobParams(TechnologyConstants.SSRS);
            lineageJob.setUploadType(GeneralConstants.UPLOAD_TYPE);

            lineageJob = jobRepo.save(lineageJob);
            jobId = lineageJob.getJobId();

            LOGGER.info("No of files in SSRS = {}"+ files.length);
            LOGGER.info("Save location for SSRS files = " + ssrsUploadLocation + jobId);

            File newUploadDirectory = new File(ssrsUploadLocation + jobId);
            newUploadDirectory.mkdir();

            File newInputDirectory = new File(ssrsInputLocation + jobId);
            newInputDirectory.mkdir();

            File newOutputDirectory = new File(ssrsOutputLocation + jobId);
            newOutputDirectory.mkdir();

            CommonUtil.uploadAllScriptsToInputLocation(files, newUploadDirectory.getAbsolutePath());
            LOGGER.info("SSRS file(s) uploaded successfully and started parsing the script(s)");

            return lineageJob;
        } catch (LineageBusinessException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            throw new LineageRuntimeException(e.getMessage());
        }
    }

    @Async
    public void parseSsrsScripts(LineageJob lineageJob, Logger LOGGER) {
        try {
            Long jobId = lineageJob.getJobId();
            String uploadedDirectory = ssrsUploadLocation + jobId;
            Path p = Paths.get(uploadedDirectory);
            String normalizedPath = Sanitization.sanitizeInput(p.normalize().toString());
            LOGGER.info("SSRS files uploaded directory: " + normalizedPath);
            File file = new File(normalizedPath);
            File[] filesArr = file.listFiles();
            if (Objects.isNull(filesArr) || filesArr.length == 0) {
                LOGGER.info("no of uploaded files in the " + uploadedDirectory + ": 0");
                return;
            }

            int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(jobId,
                    ssrsInputLocation.concat(String.valueOf(jobId)), TechnologyConstants.SSRS, lineageJob.getTechnology());
            if (exitCodeForScriptLineage != 0) {
                LOGGER.info("Script Lineage Cleansing exited with exit code: " + exitCodeForScriptLineage);
                return;
            }

            LineageJobStatus lineageJobStatus = new LineageJobStatus();
            lineageJobStatus.setJobId(lineageJob.getJobId());
            lineageJobStatus.setStepNo(2);
            lineageJobStatus.setStepName(TechnologyConstants.SSRS_SCRIPT_PARSING);
            lineageJobStatus.setStatus(GeneralConstants.STATUS_IN_PROGRESS);
            lineageJobStatus.setNoOfFileReceived(filesArr.length);
            LineageJobStatus savedJobStatus = lineageJobStatusRepository.save(lineageJobStatus);

            int noOfFilesProcessed = 0;
            for (File ssrsFile : filesArr) {
                String fileName = ssrsFile.getName().replaceAll(" ", "_");
                fileName = fileName.replaceAll("[^A-Za-z0-9,!/^[-@.\\/#&+'\\w]*$/]", "");
                fileName = fileName.replaceAll(".rdl", "");
                //Sanitization.sanitizeInput(fileName);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbFactory.newDocumentBuilder();
                Document doc = db.parse(ssrsFile);

                doc.getDocumentElement().normalize();

                LOGGER.info("Root element: " + doc.getDocumentElement().getNodeName());

                NodeList reportNodeList = doc.getElementsByTagName("Report");
                List<String> queryList = getQueriesFromFile(reportNodeList);
                LOGGER.info("No of queries in the file " + fileName + ": " + queryList.size());
                FileWriter myWriter = new FileWriter(ssrsInputLocation + jobId + File.separator + fileName);
                for (String query : queryList) {
                    myWriter.write("CREATE TABLE REPORT__" + fileName + " AS \n");
                    myWriter.write(query);
                    myWriter.write(";\n\n");
                }
                myWriter.close();
                noOfFilesProcessed++;
            }
            savedJobStatus.setNoOfFileProcessed(noOfFilesProcessed);
            savedJobStatus.setStatus(GeneralConstants.COMPLETED);
            lineageJobStatusRepository.save(savedJobStatus);

            executeLineageShellScript(ssrsInputLocation.concat(String.valueOf(jobId)),
                    ssrsOutputLocation.concat(String.valueOf(jobId)), logFileDirectory, jobId,
                    lineageJob.getTechnology(), lineageJob.getProjectName(), LOGGER);
        } catch (ParserConfigurationException pex) {
            LOGGER.info("ParserConfigurationException occurred while parsing ssrs script: " + pex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getQueriesFromFile(NodeList reportNodeList) {
        List<String> queryList = new ArrayList<>();
        for (int reportNode = 0; reportNode < reportNodeList.getLength(); reportNode++) {
            Element element1 = (Element) reportNodeList.item(reportNode);
            NodeList dataSetsNodeList = element1.getElementsByTagName("DataSets");
//            LOGGER.info("datasets length: " + dataSetsNodeList.getLength());

            for (int datasetsNode = 0; datasetsNode < dataSetsNodeList.getLength(); datasetsNode++) {
                Element element2 = (Element) dataSetsNodeList.item(datasetsNode);
                NodeList dataSetNodeList = element2.getElementsByTagName("DataSet");
//                LOGGER.info("dataset length: " + dataSetNodeList.getLength());

                for (int datasetNode = 0; datasetNode < dataSetNodeList.getLength(); datasetNode++) {
                    Element element3 = (Element) dataSetNodeList.item(datasetNode);
                    NodeList queryNodeList = element3.getElementsByTagName("Query");
//                    LOGGER.info("query length: " + queryNodeList.getLength());

                    for (int queryNode = 0; queryNode < queryNodeList.getLength(); queryNode++) {
                        Element element4 = (Element) queryNodeList.item(queryNode);
                        NodeList commandTextList = element4.getElementsByTagName("CommandText");
//                        LOGGER.info("commandText length: " + commandTextList.getLength());

                        for (int commandNode = 0; commandNode < commandTextList.getLength(); commandNode++) {
                            Element commandTextElement = (Element) commandTextList.item(commandNode);
                            String query = commandTextElement.getTextContent();
                            queryList.add(query);
                        }
                    }
                }
            }
        }
        return queryList;
    }

    public void executeLineageShellScript(String inputFileLocation, String outputFileLocation,
                                          String logFileDirectory, Long jobId,
                                          String technology, String projectName, Logger LOGGER) throws Exception {

        LOGGER.info("Job Id . . . " + jobId);

        LOGGER.info("SSRS - lineage shell script processing start . . .");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", lineageShellScriptLocation + "/" + lineageShellScript, inputFileLocation + "/",
                outputFileLocation + "/", logFileDirectory + "/", csrfToken, technology, String.valueOf(jobId));
        StringBuilder command = new StringBuilder();
        for (String commandItem : processBuilder.command()) {
            command.append(commandItem).append(" ");
        }
        LOGGER.info("Lineage Shell Script Command: " + command);
        Process process = processBuilder.start();

        // Read the script output
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        while ((line = errorReader.readLine()) != null) {
            output.append(line).append("\n");
        }
        LOGGER.info("lineage shell script execution output . . . " + output);

        // Wait for the script to finish executing and get the exit code
        int exitCodeForParsingScript = process.waitFor();
        if (exitCodeForParsingScript == 0) {
            LOGGER.info("lineage shell script executed successfully");
            executeGraphDataLoadScript(jobId, technology, projectName, LOGGER);
        } else {
            LOGGER.info("Error in executing lineage shell script");
        }

        LineageJob lineageJob = jobRepo.findById(jobId).get();
        lineageJob.setEndTime(new Date());
        lineageJob.setUploadDir(inputFileLocation);
        jobRepo.save(lineageJob);
    }

    public void executeGraphDataLoadScript(Long jobId, String technology, String projectName, Logger LOGGER) {
        try {
            technology = Sanitization.sanitizeInput(technology);
            projectName = Sanitization.sanitizeInput(projectName);

            LOGGER.info("Graph data load python processing start . . .");
            ProcessBuilder processBuilder =
                    new ProcessBuilder("python3.9", graphDataLoadScriptLocation + "/" + graphDataLoadScript,
                    csrfToken, String.valueOf(jobId), projectName, technology);

            StringBuilder command = new StringBuilder();
            for (String commandItem : processBuilder.command()) {
                command.append(commandItem).append(" ");
            }
            LOGGER.info("Graph data load Command: " + command);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            LOGGER.info("Graph data load python script execution output . . . " + output);

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOGGER.info("Graph data load python script executed successfully");
                scriptLineageService.invokeScriptLineageIdentification(jobId, projectName);
                scriptComplexity.calculateScriptComplexity(projectName, jobId.intValue(), technology,
                        technology);
            } else {
                LOGGER.info("Error in executing Graph Data Load python script with exit code: " + exitCode);
            }
            LOGGER.info("Graph Data Load python script processing end . . .");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred in executeGraphDataLoadScript" + ex.getMessage());
        }
    }
}
