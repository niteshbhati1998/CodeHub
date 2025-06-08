package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.upload.dao.DataStageDAO;
import com.cognizant.lineage.util.CommonUtil;

@Service
public class DuplicateQueryAnalysisService {
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${duplicateQueryAnalysisFileLocation}")
	private String duplicateQueryAnalysisFileLocation;
	
	@Value("${duplicateQueryAnalysisScriptLocation}")
	private String duplicateQueryAnalysisScriptLocation;
	
	@Autowired
	DataStageDAO dataStageDAO;
	
	public void uploadDuplicateQueryAnaysisFilesToServer(MultipartFile[] files, Logger LOGGER) {
		try {
			LOGGER.info("uploading duplicate query analysis files to server");
			
			LOGGER.info("removing existing files inside directory "+duplicateQueryAnalysisFileLocation);
			File f = new File(duplicateQueryAnalysisFileLocation);
			FileUtils.cleanDirectory(f); 
			LOGGER.info("existing files removed successfully");
			
			CommonUtil.uploadAllScriptsToInputLocation(files, duplicateQueryAnalysisFileLocation);
			LOGGER.info("duplicate query analysis files uploaded successfully");
		} catch (Exception ex) {
			LOGGER.info("Exception occured in executePythonScriptForDuplicateQueryAnalysis Service " + ex.getMessage());
		}
	}
	
	public void executePythonScriptForDuplicateQueryAnalysis(Logger LOGGER) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", duplicateQueryAnalysisScriptLocation, duplicateQueryAnalysisFileLocation, logFileLocation, csrfToken);
			LOGGER.info("..........Command: python3.9 "+ duplicateQueryAnalysisScriptLocation +" "+duplicateQueryAnalysisFileLocation +" "+logFileLocation +" "+csrfToken);

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("Python script executed successfully");
			} else {
				LOGGER.info("Python script failed to execute with exitCode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("Exception occurred in executePythonScriptForDuplicateQueryAnalysis Service " + ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("Exception occurred in executePythonScriptForDuplicateQueryAnalysis Service: "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}
}
