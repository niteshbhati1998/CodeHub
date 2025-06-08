package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.dao.BusinessLineageDAO;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class BusinessLineageService {
	
	@Autowired
	LoggerUtil loggerUtil;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${BusinessLineageScriptLocation}")
	private String businessLineageScriptLocation;
	
	@Value("${BusinessLineageExcelScriptLocation}")
	private String businessLineageExcelScriptLocation;
	
	@Value("${BusinessLineageExcelPath}")
	private String businessLineageExcelPath;
	
	@Autowired
	BusinessLineageDAO businessLineageDAO;

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BusinessLineageService.class);
	
	//for getting log object
    public Logger getUtilLoggerObject() {
    	Logger LOGGER = null;
    	try {
			FileHandler handler = null;
			String logFileName = null;
			synchronized (this) {
				logFileName = logFileLocation + "BusinessLineage.log";
				try {
					handler = new FileHandler(logFileName, true);
				} catch (SecurityException | IOException e) {
					throw new Exception(e);
				}
				LOGGER = loggerUtil.getLogger(handler, logFileName);
			}
    	} catch(Exception ex) {
    		LOG.info("Exception occurred in getUtilLoggerObject Service "+ex.getMessage());
    	}
		return LOGGER;
    }
    
	public List<String> getProjectNamesList(Logger LOGGER) {
		List<String> projectNamesList = new ArrayList<>();
		try {
			LOGGER.info("fetching project names list from semantic.module_wise_objects_name");
			projectNamesList = businessLineageDAO.getProjectNamesList(LOGGER);
			LOGGER.info("project names list fetched successfully");
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getProjectNamesList Service "+ex.getMessage());
		}
		return projectNamesList;
	}
	
	public String executePythonScript(String projectName, Logger LOGGER) {
		StringBuilder output = new StringBuilder();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9",businessLineageScriptLocation, csrfToken, projectName);
			LOGGER.info("..........Command: python3.9 "+ businessLineageScriptLocation+" "+csrfToken +" "+projectName);

			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line);
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("..........pythonScript executed successfully");
			} else {
				LOGGER.info("..........pythonScript failed to execute with exitCode " + exitCode);
			}
		} catch (IOException ex) {
			LOGGER.info("IOException occurred in executePythonScript Service: " + ex.getMessage());
		} catch (InterruptedException ex) {
			LOGGER.info("InterruptedException occurred in executePythonScript Service "+ex.getMessage());
			Thread.currentThread().interrupt();
		}
		return output.toString();
	}
	
	public String generateExcelReport(String projectName, Logger LOGGER) {
		String excelPath = "";
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", businessLineageExcelScriptLocation, csrfToken, projectName, businessLineageExcelPath);
			LOGGER.info("..........Command: python3.9 "+ businessLineageExcelScriptLocation+" "+csrfToken +" "+projectName +" "+businessLineageExcelPath);

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				excelPath = businessLineageExcelPath + "/" + "business_lineage_report_" + projectName + ".xlsx";
				LOGGER.info("..........pythonScript executed successfully");
			} else {
				LOGGER.info("..........pythonScript failed to execute with exitCode " + exitCode);
			}
		} catch (IOException ex) {
			LOGGER.info("IOException occurred in generateExcelReport Service: " + ex.getMessage());
		} catch (InterruptedException ex) {
			LOGGER.info("InterruptedException occurred in generateExcelReport Service "+ex.getMessage());
			Thread.currentThread().interrupt();
		}
		return excelPath;
	}
}
