package com.cognizant.lineage.upload.controller;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.BteqService;
import com.cognizant.lineage.util.Sanitization;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class BteqController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BteqController.class);

	@Autowired
	BteqService bteqService;

	@CrossOrigin
	@RequestMapping(value = "/uploadfor/{tech}/{projectName}", method = RequestMethod.POST)
	public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile[] files, 
			@PathVariable String tech, @PathVariable String projectName) {
		CommonResponse<String> response = new CommonResponse<>();
		try {
			if (Objects.isNull(files) || files.length == 0) {
				LOGGER.info("No files present in the request");
				response.setMessage(GeneralConstants.FAILED);
				response.setPayload(GeneralConstants.NO_FILES_TO_UPLOAD);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			// Sanitization
			tech = Sanitization.sanitizeInput(tech);
			projectName = Sanitization.sanitizeInput(projectName);
			
			// WhiteListing
			if(!TechnologyConstants.ALLOWED_ACTIONS.contains(tech.toLowerCase())) {   
				LOGGER.info("Exception in file upload, Technology not supported");
				response.setMessage(GeneralConstants.FAILED);
				response.setPayload(GeneralConstants.UNSUPPORTED_TECH);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			LineageJob lineageJob = bteqService.uploadFilesAndSaveLineage(files, tech, projectName);
			
			Thread.sleep(5000);
			response.setPayload(String.valueOf(lineageJob.getJobId()));
			response.setMessage(GeneralConstants.PARSING_DONE);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (InterruptedException e) {
			
			LOGGER.error("Exception in file BteqUpload: ", e);
			Thread.currentThread().interrupt();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			
		} catch (LineageBusinessException e) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			
			LOGGER.error("Exception in file BteqUpload: ", e);
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
	}
}
