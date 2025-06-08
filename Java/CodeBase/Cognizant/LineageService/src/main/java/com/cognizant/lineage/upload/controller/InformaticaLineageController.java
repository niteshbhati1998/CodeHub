package com.cognizant.lineage.upload.controller;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.InformaticaLineageService;
import com.cognizant.lineage.upload.service.JobStatusService;
import com.cognizant.lineage.util.Sanitization;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class InformaticaLineageController {

	@Autowired
	InformaticaLineageService informaticaService;
	
	@Autowired
	JobStatusService jobStatusLog;

	private static final Logger LOGGER = LoggerFactory.getLogger(InformaticaLineageController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/upload/{projectName}", method = RequestMethod.POST)
	public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile[] files, 
			@PathVariable String projectName) {
		CommonResponse<String> response = new CommonResponse<>();
		projectName = Sanitization.sanitizeInput(projectName);
		
		try {
			if (Objects.isNull(files) || files.length == 0) {
				return new ResponseEntity<String>(GeneralConstants.NO_FILES_TO_UPLOAD, 
						HttpStatus.BAD_REQUEST);
			}
			
			Map<String, Object> requiredObjects = informaticaService.uploadFilesForInformatica(files, projectName);
			LineageJob lineageJob = (LineageJob) requiredObjects.get(GeneralConstants.LINEAGE_JOB);
			Long jobId = lineageJob.getJobId();
			int filesLength = Integer.parseInt(String.valueOf(requiredObjects.get(GeneralConstants.FILE_LENGTH)));
			double size = Double.parseDouble(String.valueOf(requiredObjects.get(GeneralConstants.SIZE)));
			projectName = String.valueOf(requiredObjects.get(GeneralConstants.PROJECT_NAME));
			
			informaticaService.parseXmlFile(filesLength, size, projectName, jobId.intValue());
			
			Thread.sleep(5000);

			response.setPayload(jobId.toString());
			response.setMessage(GeneralConstants.FILES_UPLOAD_SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (InterruptedException e) {
			LOGGER.error("Exception in file upload: ", e);
			Thread.currentThread().interrupt();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			
		} catch (LineageBusinessException e) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("Exception in file upload, ", e);
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin
	@RequestMapping(value = "/parseInformaticaParameterFiles", method = RequestMethod.GET)
	public ResponseEntity<?> parseInformaticaParameterFiles() {
		CommonResponse<String> response = new CommonResponse<>();
		try {
			informaticaService.parseInformaticaParameterFiles();
			response.setMessage("Parameter files parsed successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.error("Exception occured while parsing informatica parameter files", ex.getMessage());
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin
	@RequestMapping(value = "/runStatus", method = RequestMethod.GET)
	public ResponseEntity<?> summary() {
		try { 
			String status = jobStatusLog.getJobStatus(null);
			
			CommonResponse<String> response = new CommonResponse<>();
			response.setPayload(status);
			response.setMessage(GeneralConstants.SUCCESS);
			
			HttpHeaders header = new HttpHeaders();
			header.setCacheControl(CacheControl.noCache().getHeaderValue());
			return new ResponseEntity<>(response, header, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception in summary runStatus: ", e);
			CommonResponse<String> response = new CommonResponse<>();
			response.setPayload("Unknown exception occurred. Please try after sometime");
			response.setMessage(GeneralConstants.FAILED);
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}
	}
}
