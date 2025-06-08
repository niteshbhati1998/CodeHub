package com.cognizant.lineage.pyspark.services.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.pyspark.dao.entity.MappingRecord;
import com.cognizant.lineage.pyspark.dao.entity.MethodRecord;
import com.cognizant.lineage.pyspark.dao.repository.MappingRecordRepository;
import com.cognizant.lineage.pyspark.dao.repository.MethodRecordRepository;
import com.cognizant.lineage.pyspark.model.Element;
import com.cognizant.lineage.pyspark.read.FileLoader;
import com.cognizant.lineage.pyspark.read.FileReader;
import com.cognizant.lineage.pyspark.services.api.MethodScanner;
import com.cognizant.lineage.pyspark.services.api.SourceDestinationScanner;
import com.cognizant.lineage.pyspark.util.Constants;
import com.cognizant.lineage.pyspark.util.MutableBoolean;
import com.cognizant.lineage.pyspark.util.MutableInteger;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class ProcessLine {

    private Logger LOGGER = LogManager.getLogger(ProcessLine.class);
    
    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private FileReader fileReader;

    @Autowired
    Environment environment;

    @Value("${captureDataFlowTypes}")
    private List<String> captureDataFlowTypes;
    
    @Value("${pysparkFileUploadLocation}")
    private String pysparkFileUploadLocation;

    @Autowired
    private MappingRecordRepository mappingRecordRepository;

    @Autowired
    private MethodRecordRepository methodRecordRepository;

    @Autowired
    private MethodScanner methodScanner;

    @Autowired
    private SourceDestinationScanner dataSourceScanner;

    private MutableInteger elementCount;

    @Autowired
    LineageJobRepository jobRepo;

    @Autowired
    LineageJobStatusRepository jobStatusRepository;

    public LineageJob uploadFilesToServer(MultipartFile[] files, String tech, String projectName) {
    	LineageJob lineageJob = null;
    	try {
    		tech = Sanitization.sanitizeInput(tech);
			projectName = Sanitization.sanitizeInput(projectName);
			
    		LOGGER.info("Updating status in lineage_job table");
    	    lineageJob = startJob(tech, projectName);
		
		    Long jobId = lineageJob.getJobId();		
			File newInputDirectory = new File(pysparkFileUploadLocation + File.separator + jobId);
			newInputDirectory.mkdir();
			
			LOGGER.info("uploading all files to server location");
			CommonUtil.uploadAllScriptsToInputLocation(files,newInputDirectory.getAbsolutePath());
			
			lineageJob.setUploadDir(pysparkFileUploadLocation + "/" + jobId);
			lineageJob = jobRepo.save(lineageJob);
    	} catch(Exception ex) {
    		LOGGER.info("Exception occured in uploadFilesToServer "+ex.getMessage());
    	}
		return lineageJob;
    }

    @Async
    public void processEachLine(String option, LineageJob job) {
        //LineageJob job = startJob();

        elementCount = new MutableInteger(0);
        LOGGER.info("Starting Python file processing.");

        MutableBoolean commentBlock = new MutableBoolean(false);
        String eachLine;

        //Convert properties to required format
        Iterable<MethodRecord> methodRecords = methodRecordRepository.findAll();
        Map<String, String> methodList = new HashMap();
        for(MethodRecord methodRecord : methodRecords) {
            methodList.put(methodRecord.getMethodName(), methodRecord.getQuery());
        }
        
        List<String> fileList = fileLoader.getFileLoader(job.getUploadDir());
        for (String fileName : fileList) {
            LOGGER.info("Processing File for : {}", fileName);
            fileReader.initializeReader(fileName);

            //duplicate variable count -> this will be used if same variable is being used for different assignment
            final MutableInteger varCount = new MutableInteger(1);

            MutableBoolean isMultilineCode = new MutableBoolean(false);
            MutableBoolean isMultilineString = new MutableBoolean(false);
            StringBuffer completeLine = new StringBuffer();
            int lineNo = 0;
            StringBuffer lineNos = new StringBuffer();
            List<Element> foundElements = new ArrayList<>();
            Map<String, String> stringVariableMap = new HashMap<>();
            Map<String, Element> variableNameMap = new HashMap<>(100);
            Map<String, String> overrideVariableMap = new HashMap<>();
            dataSourceScanner.reset();

            while (!(eachLine = fileReader.readNextLine()).equals(Constants.EOF)) {
                lineNo++;
                LOGGER.info(eachLine);
                boolean lineToSkip = checkLineToSkipAndConstructMultiLine(eachLine, lineNo, commentBlock, isMultilineCode, isMultilineString, completeLine, lineNos, stringVariableMap);

                if(lineToSkip) {
                    if(!isMultilineCode.getValue()) {
                        completeLine = new StringBuffer();
                        lineNos = new StringBuffer();
                    }
                    continue;
                }
                if(option.equals("util")) {
                    completeLine = new StringBuffer();
                    MethodRecord methodRecord = methodScanner.scanLine(eachLine, lineNos.toString());

                    if(methodRecord != null) {
                        methodRecord.setFolderName(fileReader.getFolderName());
                        methodRecord.setFileName(fileReader.getFileName());
                        methodRecord.setSourceCode(eachLine);
                        methodRecordRepository.save(methodRecord);
                    }
                    lineNos = new StringBuffer();
                    continue;
                }

                eachLine = completeLine.toString();
                completeLine = new StringBuffer();
                String evaluatedLineNos = lineNos.toString();
                lineNos = new StringBuffer();

                dataSourceScanner.scanLine(eachLine, evaluatedLineNos, stringVariableMap, overrideVariableMap,
                        variableNameMap, varCount, foundElements, elementCount);

            }
            Map<String, List<String>> dataMap = new HashMap<>();
            printDataMovement(foundElements, dataMap, null);
            Map<Element, List<Element>> dataMap1 = new HashMap<>();
            printDataMovement(foundElements, dataMap1, null);
            dataMap1.forEach((key, valueList) -> {
                for(Element value : valueList) {
                    String destination = null;
                    String destinationType = null;
                    String destinationLineNo = null;
                    String destinationCode = null;
                    String destinationQuery = null;
                    String list = null;
                    if (value != null) {
                        destinationType = value.getType().name();
                        destination = value.getName() ;
                        list = value.getList().toString();
                        destinationLineNo = value.getLineNos();
                        destinationCode = value.getActualCode();
                        destinationQuery = value.getQuery();
                    }

                    MappingRecord mappingRecord = new MappingRecord(job.getJobId(), fileReader.getFolderName(), fileReader.getFileName(),
                            key.getLineNos(), key.getActualCode(), key.getQuery(), key.getName(), key.getList().toString(),
                            key.getType().name(), destinationLineNo, destinationCode, destinationQuery, destination, list, destinationType);
                    mappingRecordRepository.save(mappingRecord);
                }

            });
            LOGGER.info("Processing end for: {}", fileName);
        }
        endJob(job);
    }

    private boolean validateLineToIgnore(final String line, final MutableBoolean bulkCommentStarted) {
        if (line.equals("'''")) {
            if (bulkCommentStarted.getValue()) {
                bulkCommentStarted.setValue(false);
            } else {
                bulkCommentStarted.setValue(true);
            }
            return true;
        } else if (bulkCommentStarted.getValue()) {
            return true;
        }

        if (line.startsWith("#") || line.startsWith("import")) {
            return true;
        }
        return false;
    }

    public static boolean endsWithSpecialCharacter(String input) {
        return input.endsWith(",") || input.endsWith("\\") || input.endsWith("(") || input.endsWith("[");
    }

    private static void printDataMovement(List<Element> elements, Map<String, List<String>> dataMap, String source) {
        for (Element e : elements) {
            if (e.getType().name().equals("TABLE")) {
                if (source != null) {
                    //dataMap.put(source, e.getName() + ":" + e.getList());
                    if(!dataMap.get(source).contains(e.getName() + ":" + e.getList())) {
                        dataMap.get(source).add(e.getName() + ":" + e.getList());
                    }
                } else {
                    dataMap.put(e.getName() + ":" + e.getList(), new ArrayList<>());
                }
                if (e.getNextElements().size() != 0) {
                    printDataMovement(e.getNextElements(), dataMap, e.getName() + ":" + e.getList());
                }
            } else {
                if (e.getNextElements().size() != 0) {
                    printDataMovement(e.getNextElements(), dataMap, source);
                }
            }
        }
    }

    private void printDataMovement(List<Element> elements, Map<Element, List<Element>> dataMap, Element source) {
        for (Element e : elements) {
            if (captureDataFlowTypes.contains(e.getType().name())) {
                if (source != null && dataMap.get(source) != null) {
                    if(!dataMap.get(source).contains(e)) {
                        dataMap.get(source).add(e);
                    }
                } else {
                    dataMap.put(e, new ArrayList<>());
                }
                if (e.getNextElements().size() != 0) {
                    printDataMovement(e.getNextElements(), dataMap, e);
                }
            } else {
                if (e.getNextElements().size() != 0) {
                    printDataMovement(e.getNextElements(), dataMap, source);
                }
            }
        }
    }

    private boolean checkLineToSkipAndConstructMultiLine(String originalLine, int lineNo, MutableBoolean commentBlock, MutableBoolean isMultilineCode, MutableBoolean isMultilineString, StringBuffer constructedLine, StringBuffer lineNos, Map<String, String> stringVariableMap) {
        if (!StringUtils.hasText(originalLine)) {
            return true;
        }
        if (validateLineToIgnore(originalLine, commentBlock)) {
            return true;
        }

        //for multiline code
        /**int indexOfMultilineString = originalLine.indexOf("\"\"\"");
        int nextIndexOfMultilineString = originalLine.indexOf("\"\"\"", indexOfMultilineString);
        boolean isMultipleString = false;
        if(indexOfMultilineString != -1 &&  nextIndexOfMultilineString > indexOfMultilineString)  {
            isMultipleString = true;
        }**/

        /*int nextIndexOfMultilineString = originalLine.indexOf("\"\"\"", -1);
        StringBuffer reConstructedMultiLineString = new StringBuffer();
        int startIndex = 0;
        while( nextIndexOfMultilineString != -1) {
            String str = originalLine.substring(startIndex, nextIndexOfMultilineString);
            reConstructedMultiLineString.append(" ");
            if(isMultilineString.getValue()) {
                reConstructedMultiLineString.append(str);
            } else {

                if (StringUtils.hasText(str)) {
                    if (str.contains("+")) {
                        String strAfterReplacement = str.replaceAll(Constants.SPL_CHAR_REPLACE_PATTERN, " ");
                        for (String s : strAfterReplacement.trim().split("\\+")) {
                            if (StringUtils.hasText(s)) {
                                for(String s1 : s.split(" ")) {
                                    if (stringVariableMap.containsKey(s1.trim())) {
                                        reConstructedMultiLineString.append(stringVariableMap.get(s1.trim())).append(" ");
                                    } else {
                                        reConstructedMultiLineString.append(s1).append(" ");
                                    }
                                }
                            }
                        }
                    } else {
                        reConstructedMultiLineString.append(str).append(" ");
                    }
                }
            }
            if(isMultilineString.getValue()) {
                isMultilineString.setValue(false);
            } else {
                isMultilineString.setValue(true);
            }
            startIndex = nextIndexOfMultilineString + 3;
            nextIndexOfMultilineString =  originalLine.indexOf("\"\"\"", nextIndexOfMultilineString+1);
        }*/

        /*String remainingString = originalLine.substring(startIndex, originalLine.length());
        if(StringUtils.hasText(remainingString)) {
            reConstructedMultiLineString.append(remainingString);
        }

        if(isMultilineString.getValue()) {
            if (!isMultilineCode.getValue()) {
                constructedLine.append(reConstructedMultiLineString).append(" ");
                isMultilineCode.setValue(true);
                lineNos.append(lineNo).append(":");
            } else {
                constructedLine.append(reConstructedMultiLineString).append(" ");
            }
            return true;
        } else if(StringUtils.hasText(reConstructedMultiLineString) || reConstructedMultiLineString.toString().contains(" ")) {
            constructedLine.append(reConstructedMultiLineString);
            return false;
        }*/

        int nextIndexOfMultilineString = originalLine.indexOf("\"\"\"", -1);
        StringBuffer reConstructedMultiLineString = new StringBuffer();
        int startIndex = 0;
        while( nextIndexOfMultilineString != -1) {
            String str = originalLine.substring(startIndex, nextIndexOfMultilineString);
            reConstructedMultiLineString.append(" ");
            if(isMultilineString.getValue()) {
                reConstructedMultiLineString.append(str).append("\"");
                isMultilineString.setValue(false);
            } else {
                if (StringUtils.hasText(str)) {
                    reConstructedMultiLineString.append(str);
                }
                reConstructedMultiLineString.append("\"");
                isMultilineString.setValue(true);
            }

            startIndex = nextIndexOfMultilineString + 3;
            nextIndexOfMultilineString =  originalLine.indexOf("\"\"\"", nextIndexOfMultilineString+1);
        }
        String remainingString = originalLine.substring(startIndex, originalLine.length());
        if(StringUtils.hasText(remainingString)) {
            reConstructedMultiLineString.append(remainingString);
        }

        if(isMultilineString.getValue()) {
            if (!isMultilineCode.getValue()) {
                constructedLine.append(reConstructedMultiLineString).append(" ");
                isMultilineCode.setValue(true);
                lineNos.append(lineNo).append(":");
            } else {
                constructedLine.append(reConstructedMultiLineString).append(" ");
            }
            return true;
        } /*else if(StringUtils.hasText(reConstructedMultiLineString) || reConstructedMultiLineString.toString().contains(" ")) {
            constructedLine.append(reConstructedMultiLineString);
            return false;
        }*/

        if (endsWithSpecialCharacter(originalLine)) {
            if (!isMultilineCode.getValue()) {
                constructedLine.append(originalLine);
                isMultilineCode.setValue(true);
                lineNos.append(lineNo).append(":");
            } else {
                constructedLine.append(originalLine);
            }
            return true;
        } else if (isMultilineCode.getValue()) {
            constructedLine.append(originalLine);
            isMultilineCode.setValue(false);
            lineNos.append(lineNo);
        } else {
            constructedLine.append(originalLine);
            lineNos.append(lineNo);
        }
        return false;
    }

    private LineageJob startJob(String tech, String projectName) {
		LineageJob lineageJob = new LineageJob();
		lineageJob.setProjectName(projectName);
		lineageJob.setTechnology(tech);
		lineageJob.setStartTime(new Date());
		lineageJob.setJobParams("PySpark");
		lineageJob.setUploadType(GeneralConstants.UPLOAD_TYPE);

        lineageJob = jobRepo.save(lineageJob);
        Long jobId = lineageJob.getJobId();
        LOGGER.info("Job id : {} started at {}", lineageJob.getJobId(), lineageJob.getStartTime());
        return lineageJob;
    }

    private LineageJob endJob(LineageJob currentJob) {
        Date completionTime = new Date();
        currentJob.setEndTime(completionTime);
        jobRepo.save(currentJob);
        long timeTaken = completionTime.getTime() - currentJob.getStartTime().getTime();
        LOGGER.info("Job id : {} completed, total time taken {}", currentJob.getJobId(), timeTaken);
        return currentJob;
    }
}