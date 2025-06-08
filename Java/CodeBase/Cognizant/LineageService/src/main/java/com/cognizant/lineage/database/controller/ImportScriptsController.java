package com.cognizant.lineage.database.controller;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.database.dao.ImportScriptsDAO;
import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.database.model.ScriptConfigurationDetails;
import com.cognizant.lineage.database.model.ServerHostRequest;
import com.cognizant.lineage.database.service.ImportScriptsService;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class ImportScriptsController {

	@Autowired
	LoggerUtil loggerUtil;
	
	@Value("${logFileLocation}")
	private String logFileLocation;

	@Autowired
	ImportScriptsService importScriptsService;
	
	@Autowired
	ImportScriptsDAO importScriptsDAO;
  
	@PostMapping("/fetchServerHosts")
	public ResponseEntity<?> fetchServerHosts(@RequestBody ServerHostRequest serverHostRequest) throws Exception {		
		final String scriptType = Sanitization.sanitizeInput(serverHostRequest.getScriptType());
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ScriptConfigurationDropDown" + ".log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}	
		try {
			Map<String, String> hostIpDetails = importScriptsService.fetchServerHosts(scriptType,LOGGER);
			CommonResponse<Map<String, String>> res = new CommonResponse<>();
			res.setMessage("Host Set for script type fetched successfully");
			res.setPayload(hostIpDetails);
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in fetchServerHosts Controller "+ex.getMessage());
			CommonResponse<String> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred. Please try again after some time");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);		}
	}

	@CrossOrigin
	@PostMapping("/captureLineage")
	public ResponseEntity<?> importScript(@RequestBody ScriptConfigurationDetails scriptConfigurationDetails) throws Exception {
		CommonResponse<String> response = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ScriptConfigurationCaptureLineage" + ".log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		ScriptConfigurationDetails sanitizedScriptConfigurationDetails = sanitizedScriptConfigurationDetails(
				scriptConfigurationDetails);
		try {
			LOGGER.info("start.........................................................................................................");
			LOGGER.info("Step1: start calling python import script");
			Sanitization.sanitizeDirectory(scriptConfigurationDetails.getInputDirectory());
			Sanitization.sanitizeDirectory(scriptConfigurationDetails.getOutputDirectory());
			String jobId = String.valueOf(importScriptsDAO.getJobId(LOGGER));
			importScriptsService.executePythonImportScript(sanitizedScriptConfigurationDetails, jobId, logFileName, LOGGER);
			response.setMessage("ImportScript executed successfully");
			response.setPayload(jobId);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (LineageBusinessException e) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred. Please try again after some time");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ScriptConfigurationDetails sanitizedScriptConfigurationDetails(
			ScriptConfigurationDetails scriptConfigurationDetails) {
		ScriptConfigurationDetails sanitizedScriptConfigurationDetails = new ScriptConfigurationDetails();
		sanitizedScriptConfigurationDetails
				.setProjectName(Sanitization.sanitizeInput(scriptConfigurationDetails.getProjectName()));
		sanitizedScriptConfigurationDetails
				.setScriptName(Sanitization.sanitizeInput(scriptConfigurationDetails.getScriptName()));
		sanitizedScriptConfigurationDetails
				.setServerAlias(Sanitization.sanitizeInput(scriptConfigurationDetails.getServerAlias()));
		sanitizedScriptConfigurationDetails
				.setInputDirectory(Sanitization.sanitizeInput(scriptConfigurationDetails.getInputDirectory()));
		sanitizedScriptConfigurationDetails
				.setFileExtension(Sanitization.sanitizeInput(scriptConfigurationDetails.getFileExtension()));
		sanitizedScriptConfigurationDetails
				.setOutputDirectory(Sanitization.sanitizeInput(scriptConfigurationDetails.getOutputDirectory()));
		return sanitizedScriptConfigurationDetails;

	}
}