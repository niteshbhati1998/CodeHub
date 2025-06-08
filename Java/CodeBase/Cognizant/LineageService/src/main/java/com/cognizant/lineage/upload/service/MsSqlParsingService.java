package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.database.service.ImportDBObjectsService;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class MsSqlParsingService {

    @Value("${csrfToken}")
    private String csrfToken;

    @Value("${logFileLocation}")
    private String logFileLocation;

    @Value("${sqlServerFileInputLocation}")
    private String uploadFileInputLocation;

    @Value("${sqlServerFileOutputLocation}")
    private String uploadFileOutputLocation;

    @Value("${bteqScriptLocation}")
    private String scriptLocationAllStepsStartingFromCleansing;

    @Value("${bteqScriptName}")
    private String scriptNameAllStepsStartingFromCleansing;

    @Value("${commonScriptLocation}")
    private String commonScriptLocation;

    @Value("${bteqPythonScriptName}")
    private String bteqPythonScriptName;

    @Autowired
    LineageJobRepository jobRepo;

    @Autowired
    ScriptLineageService scriptLineageService;

    @Autowired
    ScriptComplexity scriptComplexity;

    public LineageJob uploadFilesAndSaveLineage(MultipartFile[] files, String tech, String projectName,
                                                Logger LOGGER) throws LineageBusinessException {
        try {

            Long jobId = jobRepo.getJobIdByMax();

            LineageJob lineageJob = new LineageJob();
            lineageJob.setJobId(jobId);
            lineageJob.setProjectName(projectName);
            lineageJob.setTechnology(tech);
            lineageJob.setStartTime(new Date());
            lineageJob.setJobParams(TechnologyConstants.MS_SQL_SERVER);
            lineageJob.setUploadType(GeneralConstants.UPLOAD_TYPE);

            lineageJob = jobRepo.save(lineageJob);
            jobId = lineageJob.getJobId();

            String msSqlServerUploadInputLocation = uploadFileInputLocation + File.separator + jobId;

            LOGGER.info("No of files in Bteq = " + files.length);
            LOGGER.info("Save location for Bteq = " + msSqlServerUploadInputLocation);

            File newInputDirectory = new File(msSqlServerUploadInputLocation);
            newInputDirectory.mkdir();

            File newOutputDirectory = new File(uploadFileOutputLocation + File.separator + jobId);
            newOutputDirectory.mkdir();

            CommonUtil.uploadAllScriptsToInputLocation(files,newInputDirectory.getAbsolutePath());
            LOGGER.info("MS SQl Server Uploaded files successfully and invoked shell script for processing");

//            importDBObjectsService.executePythonScriptStartingFromCleansing(uploadFileInputLocation, uploadFileOutputLocation,
//                    logFileLocation, Integer.parseInt(String.valueOf(jobId)), TechnologyConstants.MS_SQL_SERVER, projectName,
//                    tech, LOGGER);

            return lineageJob;

        } catch (LineageBusinessException e) {
            throw e;
        } catch (Exception e) {
//            e.printStackTrace();
            LOGGER.info("Exception occurred in uploadFilesAndSaveLineage: " + e.getMessage());
            throw new LineageRuntimeException(e.getMessage());
        }
    }

    @Async
    public void executePythonScriptsForSqlServer(String projectName, String tech, Long jobId, Logger LOGGER) {
        try {
            String msSqlServerUploadInputLocation = uploadFileInputLocation + File.separator + jobId;
            String techForScriptInvocation = "mssqlserver" + tech;

            int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(jobId,
                    msSqlServerUploadInputLocation, TechnologyConstants.MS_SQL_SERVER, tech);
            if (exitCodeForScriptLineage != 0) {
                LOGGER.info("Script Lineage exited with exit code: " + exitCodeForScriptLineage);
                return;
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash",
                    scriptLocationAllStepsStartingFromCleansing + "/" + scriptNameAllStepsStartingFromCleansing,
                    uploadFileInputLocation + "/" + jobId, uploadFileOutputLocation + "/" + jobId,
                    logFileLocation, csrfToken, techForScriptInvocation.toLowerCase(), String.valueOf(jobId));
            LOGGER.info("bash " + scriptLocationAllStepsStartingFromCleansing + "/" +
                    scriptNameAllStepsStartingFromCleansing + " " + uploadFileInputLocation + "/" + jobId + " " +
                    uploadFileOutputLocation + "/" + jobId + " " + logFileLocation + " " + csrfToken + " " +
                    techForScriptInvocation.toLowerCase() + " " + jobId);
            Process process = processBuilder.start();
            LOGGER.info(".........2nd python script JobId: " + jobId);
            LOGGER.info(".........2nd python script Name: " + scriptNameAllStepsStartingFromCleansing);
            LOGGER.info(".........2nd python script Location: " + scriptLocationAllStepsStartingFromCleansing);

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOGGER.info(".........received exit code 0");
                LOGGER.info(".........Step3(1): start calling 3rd python script for performing last step");
                LOGGER.info("Output for the script: " + output);
                executeScriptForLastStep(jobId, tech, projectName, LOGGER);
                LOGGER.info(".........Step3(1): last step script executed successfully");
            } else {
                LOGGER.info(".........received exit code " + exitCode);
            }
        } catch (IOException ex1) {
            LOGGER.info("IOException occurred in executePythonScriptsForSqlServer Service " + ex1.getMessage());
        } catch (InterruptedException ex2) {
            LOGGER.info("InterruptedException occurred in executePythonScriptsForSqlServer Service " + ex2.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void executeScriptForLastStep(long jobId, String technology, String projectName, Logger LOGGER)
            throws IOException, InterruptedException {

        String technologyForScript = "mssqlserver" + technology;

        technology = Sanitization.sanitizeInput(technology);
        projectName = Sanitization.sanitizeInput(projectName);

        ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + bteqPythonScriptName,
                csrfToken, String.valueOf(jobId),  projectName, technologyForScript.toLowerCase());
        LOGGER.info("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "
                +csrfToken +" "+jobId +" "+projectName+" "+technologyForScript.toLowerCase());
        Process process = processBuilder2.start();
        LOGGER.info(".........jobId: "+jobId);
        LOGGER.info(".........3rd pythonscript Name: "+bteqPythonScriptName);
        LOGGER.info(".........3rd pythonscript Location: "+commonScriptLocation);
        LOGGER.info(".........sending technology to config file: "+technologyForScript.toLowerCase());

        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            LOGGER.info(bteqPythonScriptName + " exited exit code 0 " + "and output: " + output);
            scriptLineageService.invokeScriptLineageIdentification(jobId, projectName);
            scriptComplexity.calculateScriptComplexity(projectName, (int) jobId, technology, TechnologyConstants.MS_SQL_SERVER);
        } else {
            LOGGER.info(bteqPythonScriptName + "exited with exit code " + exitCode  + "and output: " + output);
        }
    }
}
