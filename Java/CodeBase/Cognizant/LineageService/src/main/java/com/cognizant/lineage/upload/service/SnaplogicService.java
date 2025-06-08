package com.cognizant.lineage.upload.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.entity.SnaplogicInfo;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.dao.repository.SnaplogicInfoRepo;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.SLConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.SnaplogicUploadRequest;
import com.cognizant.lineage.util.CommonUtil;

@Service
public class SnaplogicService {

    @Value("${snaplogicUploadLocation}")
    private String snaplogicUploadLocation;

    @Autowired
    LineageJobRepository jobRepo;

    @Autowired
    LineageJobStatusRepository jobStatusRepository;

    @Autowired
    SnaplogicInfoRepo snaplogicInfoRepo;

    @Autowired
    ExecuteScript executeScript;

    @Autowired
    ScriptLineageService scriptLineageService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SnaplogicService.class);

    public LineageJob uploadSnaplogicFiles(SnaplogicUploadRequest uploadRequest) {
        try {
            Long jobId = jobRepo.getJobIdByMax();

            LineageJob lineageJob = new LineageJob();
            lineageJob.setJobId(jobId);
            lineageJob.setProjectName(uploadRequest.getProjectName());
            lineageJob.setTechnology(uploadRequest.getTech());
            lineageJob.setStartTime(new Date());
            lineageJob.setJobParams(uploadRequest.getTech());
            lineageJob.setUploadType(GeneralConstants.UPLOAD_TYPE);

            lineageJob = jobRepo.save(lineageJob);
            jobId = lineageJob.getJobId();

            LOGGER.info("No of files in Snaplogic: {}", uploadRequest.getFile().length);
            LOGGER.info("Save location for Snaplogic files: " + snaplogicUploadLocation + "/" + jobId);

            File newInputDirectory = new File(snaplogicUploadLocation + File.separator + jobId);
            newInputDirectory.mkdir();

            CommonUtil.uploadAllScriptsToInputLocation(uploadRequest.getFile(), newInputDirectory.getAbsolutePath());

            return lineageJob;
        } catch (LineageBusinessException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void parseSnaplogicFiles(LineageJob lineageJob) {
        Long jobId = lineageJob.getJobId();
        List<LineageJobStatus> jobStatusList = jobStatusRepository.findAllByJobId(jobId);
        try {
            File folder = new File(snaplogicUploadLocation + "/" + jobId);
            File[] listOfFiles = folder.listFiles();
            if (Objects.isNull(listOfFiles) || listOfFiles.length == 0) {
                LOGGER.info("No files are present for jobId: {}", jobId);
                return;
            }

            int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(jobId,
                    snaplogicUploadLocation + "/" + jobId,
                    lineageJob.getJobParams(), lineageJob.getTechnology());
            if (exitCodeForScriptLineage != 0) {
                LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
                return;
            }

            LineageJobStatus jobStatus1 = prepareLineageJobStatus(jobId, listOfFiles.length);
            jobStatusRepository.save(jobStatus1);

            LineageJobStatus jobStatus = jobStatusList.isEmpty() ?
                    prepareLineageJobStatus(jobId, listOfFiles.length) : jobStatusList.get(jobStatusList.size()-1);
            int noOfFileProcessed = processFiles(jobId, jobStatus, listOfFiles);
            if (noOfFileProcessed != listOfFiles.length) {
                jobStatus.setJobStatusDetails("Some files are not processed because they doesn't contain snap map");
            }
            jobStatus.setNoOfFileProcessed(noOfFileProcessed);
            jobStatus.setStatus(GeneralConstants.COMPLETED);
            jobStatusRepository.save(jobStatus);

            int exitCodeForLineageIdentificationScript = executeScript.executePythonBteqScript(jobId.toString(),
                    TechnologyConstants.SNAPLOGIC.toLowerCase(), lineageJob.getProjectName());

            if (exitCodeForLineageIdentificationScript == 0) {
                LOGGER.info("Lineage identification script executed successfully");
                scriptLineageService.invokeScriptLineageIdentification(jobId, lineageJob.getProjectName());
            } else {
                LOGGER.info("Error occurred in Lineage identification script");
            }
        } catch (Exception e) {
            LineageJobStatus jobStatus = jobStatusList.get(0);
            jobStatus.setStatus(GeneralConstants.ERROR);
            jobStatusRepository.save(jobStatus);
            LOGGER.info("Exception occurred while processing snaplogic files: {}", e.getMessage());
        }
    }

    private Integer processFiles(Long jobId, LineageJobStatus jobStatus, File[] listOfFiles) {
        int noOfFileProcessed = 0;
        try {
            for (File file : listOfFiles) {
                InputStream is = new FileInputStream(file.getAbsolutePath());
                String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);

                JSONObject jsonRoot = new JSONObject(jsonTxt);

                JSONObject jsonForSnapMap = jsonRoot.getJSONObject(SLConstants.KEY_SNAP_MAP);
                if (Objects.isNull(jsonForSnapMap)) {
                    LOGGER.info("Snaplogic Snap map is not present. Skipping execution for file: {}", file.getName());
                    continue;
                }
                StringBuilder sqlStatements = new StringBuilder();
                for (String key : jsonForSnapMap.keySet()) {
                    JSONObject jsonForKey = jsonForSnapMap.getJSONObject(key);
                    String classId = jsonForKey.getString(SLConstants.KEY_CLASS_ID);
                    if (Objects.isNull(classId)) {
                        LOGGER.info("Snaplogic Class Id is not present. Skipping execution for key: {}", key);
                        continue;
                    }
                    switch (classId) {
                        case SLConstants.SNAP_FLOW_PIPE_EXEC:
                            processSnapFlowPipeExec(jobId, key, classId, jsonForKey, file.getName());
                            break;
                        case SLConstants.SNAP_REDSHIFT_EXECUTE:
                            processSnapRedshiftExecute(key, jsonForKey, sqlStatements);
                            break;
                        default:
                            break;
                    }
                }
                String parameters = fetchParametersFromPropertyMap(jsonRoot, file.getName());
                snaplogicInfoRepo.updateParametersAndSqlStatementForSource(parameters, sqlStatements.toString(), jobId,
                        file.getName().replace(".json", "").toUpperCase());
                noOfFileProcessed++;
            }
        } catch (IOException e) {
            jobStatus.setJobStatusDetails(e.getMessage());
            jobStatus.setStatus(GeneralConstants.ERROR);
            jobStatusRepository.save(jobStatus);
            LOGGER.info("IOException occurred while parsing snaplogic files: {}", e.getMessage());
        } catch (Exception e) {
            jobStatus.setStatus(GeneralConstants.ERROR);
            jobStatusRepository.save(jobStatus);
            LOGGER.info("Exception occurred while processing snaplogic files: {}", e.getMessage());
        }
        return noOfFileProcessed;
    }

    private void processSnapFlowPipeExec(Long jobId, String key, String classId, JSONObject jsonForKey, String fileName) {
        try {
            JSONObject jsonForPropertyMap = jsonForKey.getJSONObject(SLConstants.KEY_PROPERTY_MAP);
            if (Objects.isNull(jsonForPropertyMap)) {
                LOGGER.info("Snaplogic Property Map is not present. Skipping execution for key: {}", key);
                return;
            }
            JSONObject jsonForSettings = jsonForPropertyMap.getJSONObject(SLConstants.KEY_SETTINGS);
            if (Objects.isNull(jsonForSettings)) {
                LOGGER.info("Snaplogic Settings is not present. Skipping execution for key: {}", key);
                return;
            }
            JSONObject jsonForPipeline = jsonForSettings.getJSONObject(SLConstants.KEY_PIPELINE);
            JSONObject jsonForExecutionMode = jsonForSettings.getJSONObject(SLConstants.KEY_EXECUTION_MODE);
            JSONObject jsonForSnapLex = jsonForSettings.getJSONObject(SLConstants.KEY_SNAPLEX);
            if (Objects.isNull(jsonForPipeline) || Objects.isNull(jsonForExecutionMode)) {
                LOGGER.info("Snaplogic Pipeline/Execution Mode is not present. Skipping execution for key: {}", key);
                return;
            }
            String pipeLineSource = fileName.replace(SLConstants.JSON_EXTENSION, "").toUpperCase();
            String pipeLineTarget = jsonForPipeline.getString(SLConstants.KEY_VALUE).toUpperCase();
            String executionMode = jsonForExecutionMode.getString(SLConstants.KEY_VALUE);
            String snaplexPath = jsonForSnapLex.get(SLConstants.KEY_VALUE) instanceof String ?
                    jsonForSnapLex.getString(SLConstants.KEY_VALUE) : "";

            SnaplogicInfo snaplogicInfo = new SnaplogicInfo();
            snaplogicInfo.setJobId(jobId);
            snaplogicInfo.setFileName(fileName);
            snaplogicInfo.setSource(pipeLineSource);
            snaplogicInfo.setTarget(pipeLineTarget);
            snaplogicInfo.setSnaplexPath(snaplexPath);
            snaplogicInfo.setExecutionMode(executionMode);
            snaplogicInfo.setClassId(classId);

            snaplogicInfoRepo.save(snaplogicInfo);

        } catch (Exception e) {
            LOGGER.info("Exception occurred while parsing snaplogic file pipe_exec property_map for key: {} :: {}",
                    key, e.getMessage());
        }
    }

    private String prepareParamsFromJson(JSONObject jsonForParams, String key) {
        try {
            if (Objects.isNull(jsonForParams)) {
                LOGGER.info("Snaplogic Params is not present for key: {}", key);
                return "";
            }
            JSONArray jsonForParamValue = jsonForParams.getJSONArray(SLConstants.KEY_VALUE);
            if (Objects.isNull(jsonForParamValue) || jsonForParamValue.isEmpty()) {
                LOGGER.info("Snaplogic Params Value is not present for key: {}", key);
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < jsonForParamValue.length(); i++) {
                JSONObject jsonParamObject = jsonForParamValue.getJSONObject(i);
                JSONObject jsonForParamName = jsonParamObject.getJSONObject(SLConstants.KEY_PARAM_NAME);
                sb.append(jsonForParamName.getString(SLConstants.KEY_VALUE)).append(",");
            }
            return sb.substring(0, sb.length() - 1);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching params for key: {} :: {}",key, ex.getMessage());
        }
        return "";
    }

    private LineageJobStatus prepareLineageJobStatus(Long jobId, Integer noOfFilesReceived) {
        LineageJobStatus jobStatus = new LineageJobStatus();
        jobStatus.setJobId(jobId);
        jobStatus.setStepNo(2);
        jobStatus.setStepName(TechnologyConstants.SCRIPT_PARSING);
        jobStatus.setNoOfFileReceived(noOfFilesReceived);
        jobStatus.setStatus(GeneralConstants.STATUS_IN_PROGRESS);
        return jobStatus;
    }

    private void processSnapRedshiftExecute(String key, JSONObject jsonForKey, StringBuilder sqlStatements) {
        try {
            JSONObject jsonForPropertyMap = jsonForKey.getJSONObject(SLConstants.KEY_PROPERTY_MAP);
            if (Objects.isNull(jsonForPropertyMap)) {
                LOGGER.info("Snaplogic Redshift Execute Property Map is not present for key: {}", key);
                return;
            }
            JSONObject jsonForSettings = jsonForPropertyMap.getJSONObject(SLConstants.KEY_SETTINGS);
            if (Objects.isNull(jsonForSettings)) {
                LOGGER.info("Snaplogic Redshift Execute Settings is not present for key: {}", key);
                return;
            }
            JSONObject jsonForSqlStatement = jsonForSettings.getJSONObject(SLConstants.KEY_SQL_STATEMENT);
            if (Objects.isNull(jsonForSqlStatement)) {
                LOGGER.info("Snaplogic Redshift Execute Sql Statement is not present for key: {}", key);
                return;
            }
            sqlStatements.append(jsonForSqlStatement.getString(SLConstants.KEY_VALUE)).append(";");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while processing snaplogic file redshift-execute property_map for key: {} :: {}",
                    key, ex.getMessage());
        }
    }

    private Map<String, String> prepareSourceAndDestLinkMap(JSONObject jsonForLinkMap) {
        Map<String, String> sourceAndDestLinkMap = new HashMap<>();
        try {
            Set<String> linkKeySet = jsonForLinkMap.keySet();
            for (String linkKey : linkKeySet) {
                JSONObject jsonForLinkKey = jsonForLinkMap.getJSONObject(linkKey);
                sourceAndDestLinkMap.put(jsonForLinkKey.getString(SLConstants.KEY_SRC_ID),
                        jsonForLinkKey.getString(SLConstants.KEY_DEST_ID));
            }
        } catch (Exception e) {
            LOGGER.info("Exception occurred while processing snaplogic files for link map: {}", e.getMessage());
        }
        return sourceAndDestLinkMap;
    }

    private void setSqlStatementForTarget(Long jobId, String source,
                                          Map<String, String> keyAndSqlStatementMap,
                                          Map<String, String> keyAndSnapTypeMap,
                                          Map<String, String> sourceAndDestLinkMap,
                                          Map<String, String> pipelineIdAndNameMap) {
        source = source.replace(SLConstants.JSON_EXTENSION, "").toUpperCase();
        Map<String, String> destPipelineForSqlStatement = new HashMap<>();
        for (Map.Entry<String, String> entry : keyAndSqlStatementMap.entrySet()) {
            boolean isDestinationPipelineFound = false;
            String sourceId = entry.getKey();
            while (!isDestinationPipelineFound) {
                String destinationId = sourceAndDestLinkMap.get(sourceId);
                if (StringUtils.isEmpty(destinationId)) {
                    isDestinationPipelineFound = true;
                    LOGGER.info("Snaplogic destination not found for redshift execute key: {}", entry.getKey());
                }
                String destinationType = keyAndSnapTypeMap.get(destinationId);
                if (!StringUtils.isEmpty(destinationType) &&
                        SLConstants.SNAP_FLOW_PIPE_EXEC.equalsIgnoreCase(destinationType)) {
                    destPipelineForSqlStatement.put(destinationType, entry.getValue());
                    System.out.println("Sql Statement: " + entry.getValue() + ":: source: " + source + ":: target: " + pipelineIdAndNameMap.get(destinationId) );
//                    snaplogicInfoRepo.updateSqlStatementForSourceAndTarget(entry.getValue(), jobId,
//                            source, pipelineIdAndNameMap.get(destinationId));
                    isDestinationPipelineFound = true;
                }
                sourceId = destinationId;
            }
        }
    }

    private String fetchParametersFromPropertyMap(JSONObject jsonRoot, String fileName) {
        StringBuilder parameters = new StringBuilder();
        try {
            JSONObject jsonForPropertyMap = jsonRoot.getJSONObject(SLConstants.KEY_PROPERTY_MAP);
            if (Objects.isNull(jsonForPropertyMap)) {
                LOGGER.info("Snaplogic property map is not present for file: {}", fileName);
                return parameters.toString();
            }
            JSONObject jsonForSettings = jsonForPropertyMap.getJSONObject(SLConstants.KEY_SETTINGS);
            if (Objects.isNull(jsonForSettings)) {
                LOGGER.info("Snaplogic property map - settings is not present for file: {}", fileName);
                return parameters.toString();
            }
            JSONObject jsonForParamTable = jsonForSettings.getJSONObject(SLConstants.KEY_PARAM_TABLE);
            if (Objects.isNull(jsonForParamTable)) {
                LOGGER.info("Snaplogic property map - settings - param table is not present for file: {}", fileName);
                return parameters.toString();
            }
            JSONArray jsonForValue = jsonForParamTable.getJSONArray(SLConstants.KEY_VALUE);
            for (int i = 0; i < jsonForValue.length(); i++) {
                JSONObject object = jsonForValue.getJSONObject(i);
                JSONObject jsonForKey = object.getJSONObject(SLConstants.KEY_KEY);
                String key = jsonForKey.getString(SLConstants.KEY_VALUE);
                JSONObject jsonForKeyValue = object.getJSONObject(SLConstants.KEY_VALUE);
                String value = jsonForKeyValue.getString(SLConstants.KEY_VALUE);
                parameters.append(key).append(":").append(value).append(",");
            }
        } catch (Exception ex) {
            LOGGER.info("Snaplogic parsing exception occurred while fetching parameters for file: {} :: {}", fileName, ex.getMessage());
        }
        return parameters.length() > 0 ? parameters.substring(0, parameters.length() - 1) : parameters.toString();
    }
}
