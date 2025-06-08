package com.cognizant.lineage.upload.controller;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.upload.model.ResponseForDB;
import com.cognizant.lineage.upload.service.DuplicateQueryAnalysisService;
import com.cognizant.lineage.util.LoggerUtil;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class DuplicateQueryAnalysisController {
	
	@Autowired
    LoggerUtil loggerUtil;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Autowired
	DuplicateQueryAnalysisService duplicateQueryAnalysisService;

	@CrossOrigin
	@PostMapping("/duplicate/upload")
	public ResponseEntity<?> uploadDuplicateQueryAnalysisFiles(@RequestParam("file") MultipartFile[] files) throws Exception {
		ResponseForDB response = new ResponseForDB();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "DuplicateQueryAnalysis.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		LOGGER.info("start..................................................................");
		try {
			duplicateQueryAnalysisService.uploadDuplicateQueryAnaysisFilesToServer(files, LOGGER);
			duplicateQueryAnalysisService.executePythonScriptForDuplicateQueryAnalysis(LOGGER);
			response.setMessage("Duplicate Query Analysis Files Uploaded Successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred in Duplicate Query Analysis Controller");
			LOGGER.info("Exception occurred in uploadDuplicateQueryAnalysisFiles Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}
}