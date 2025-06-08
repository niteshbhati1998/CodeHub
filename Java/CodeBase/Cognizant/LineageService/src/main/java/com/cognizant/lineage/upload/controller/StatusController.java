package com.cognizant.lineage.upload.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.JobStatusList;
import com.cognizant.lineage.upload.model.LineageJobStatusUI;
import com.cognizant.lineage.upload.model.LineageResponse;
import com.cognizant.lineage.upload.service.JobStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class StatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusController.class);
    
    @Autowired
    private JobStatusService jobStatusService;

    @CrossOrigin
    @GetMapping (value = "/execution/status/log/{jobId}")
    public ResponseEntity<?> getExecutionStatusLog(@PathVariable(required = true) Long jobId) {
        try {
            String status = jobStatusService.getJobStatus(jobId);
            LineageResponse response = new LineageResponse();
            if(status.contains(GeneralConstants.PROCESSED_FOR_PROJECT_STRING) &&
                    status.contains(GeneralConstants.COMPLEXITY_CALCULATION_COMPLETED_STATUS_STRING)) {
            	response.setStatusMessage(GeneralConstants.COMPLETED);
            } else {
            	response.setStatusMessage(GeneralConstants.STATUS_IN_PROGRESS);
            }
            response.setPayload(status);

            HttpHeaders header = new HttpHeaders();
            header.setCacheControl(CacheControl.noCache().getHeaderValue());
            return new ResponseEntity<>(response, header, HttpStatus.OK);
        } catch (LineageBusinessException e) {
            LOGGER.info("Job id not found for: {}", jobId);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Job Id not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Exception in get execution status: ", e);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping (value = "/execution/status/jobs")
    public ResponseEntity<?> getAllLineageJobs() {
    	try {
    		List<LineageJobStatusUI> lineageJobs = jobStatusService.getAllLineageJobs();
    		CommonResponse<List<LineageJobStatusUI>> response = new CommonResponse<>();
    		response.setMessage("Jobs found");
    		response.setPayload(lineageJobs);
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	} catch (LineageBusinessException ex) {
    		LOGGER.error("Exception in getting lineageJobs : ", ex);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload(ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    	} catch (Exception ex) {
    		LOGGER.error("Exception in getting lineageJobs : ", ex);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server error occurred. Please try again after some time");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    }

    @GetMapping (value = "/execution/status/{jobId}")
    public ResponseEntity<?> getExecutionStatus(@PathVariable("jobId") Long jobId) {
    	try {
    		if (Objects.isNull(jobId)) {
    			return new ResponseEntity<>("Job Id is required", HttpStatus.BAD_REQUEST);
    		}
    		JobStatusList jobStatusList = jobStatusService.getExecutionStatus(jobId);
    		ObjectMapper mapper = new ObjectMapper();
    		String jobStatusString = mapper.writeValueAsString(jobStatusList);
    		CommonResponse<JobStatusList> response = new CommonResponse<>();
    		if (jobStatusString.contains(GeneralConstants.PROCESSED_FOR_PROJECT_STRING) &&
                    jobStatusString.contains(GeneralConstants.COMPLEXITY_CALCULATION_COMPLETED_STATUS_STRING)) {
            	response.setMessage(GeneralConstants.COMPLETED);
            } else {
            	response.setMessage(GeneralConstants.STATUS_IN_PROGRESS);
            }
    		response.setPayload(jobStatusList);
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	} catch (LineageBusinessException ex) {
    		LOGGER.error("Exception in getting lineageJobs : ", ex);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload(ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
    		LOGGER.error("Exception in getting ExecutionStatusLog : ", ex);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server error occurred. Please try again after some time");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/extractStatus/log")
    public ResponseEntity<?> getDbUploadExecutionStatus(@RequestBody HashMap<String, String> jobIdWithTechMap) {
        try {
            CommonResponse<String> response = new CommonResponse<>();
            String dbUploadStatus = jobStatusService.getDbUploadStatus(jobIdWithTechMap);
            int noOfOccurrences = jobStatusService.getNoOfOccurrences(dbUploadStatus);
            if (noOfOccurrences == jobIdWithTechMap.keySet().size()) {
                response.setMessage(GeneralConstants.COMPLETED);
            } else {
                response.setMessage(GeneralConstants.STATUS_IN_PROGRESS);
            }
            response.setPayload(dbUploadStatus);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception occurred: ", ex);
            CommonResponse<String> response = new CommonResponse<>();
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server error occurred. Please try again after some time");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
