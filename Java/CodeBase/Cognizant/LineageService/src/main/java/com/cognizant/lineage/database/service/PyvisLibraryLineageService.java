package com.cognizant.lineage.database.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.database.constants.Constants;
import com.cognizant.lineage.database.dao.PyvisLibraryLineageDAO;
import com.cognizant.lineage.database.model.PythonScriptInput;
import com.cognizant.lineage.database.model.UIInput3;
import com.cognizant.lineage.database.model.UIInput4;
import com.cognizant.lineage.util.Sanitization;

@Service
public class PyvisLibraryLineageService {
	
	@Value("${pyvisLibraryScriptNameNodeOrScript}")
	private String pyvisLibraryScriptNameNodeOrScript;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${pyvisLibraryScriptNameTech}")
	private String pyvisLibraryScriptNameTech;
	
	@Value("${excelGenerationScriptLocation}")
	private String excelGenerationScriptLocation;
	
	@Value("${pyvisDisplayTableLineageScript}")
	private String pyvisDisplayTableLineageScript;
	
	@Value("${pythonScriptLocation}")
	private String pythonScriptLocation;

	@Value("${knowledgeGraphScript}")
	private String knowledgeGraphScript;

	@Value("${csrfToken}")
	private String csrfToken;

	@Value("${technicalLineageReportFileLocation}")
	private String technicalLineageReportFileLocation;

    @Autowired
    PyvisLibraryLineageDAO pyvisLibraryLineageDAO;
	  
	private static final Logger LOGGER = LoggerFactory.getLogger(PyvisLibraryLineageService.class);

	public List<String> getFilterDropdownList(String projectName, String type, Integer pageNo) {
		List<String> filterDropdownList = new ArrayList<>();
		try {
			filterDropdownList = pyvisLibraryLineageDAO.getFilterDropdownList(projectName,type);
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in getFilterDropdownList Service " + ex.getMessage());
		}
		int size = filterDropdownList.size();
		return filterDropdownList.subList((pageNo * 500) - 500, Math.min(pageNo * 500, size));
	}
	
	public List<String> getTableNames(String projectName, String scriptType) {
		List<String> tableNamesList = new ArrayList<>();
		try {
			tableNamesList = pyvisLibraryLineageDAO.getTableNames(projectName,scriptType);
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in getTableNames Service " + ex.getMessage());
		}
		return tableNamesList;
	}
	
	public String executePyvisLibraryScript(UIInput3 uiInput) {
		StringBuilder sb = new StringBuilder();
		ProcessBuilder processBuilder = new ProcessBuilder();
		try {
			   String projectName = Sanitization.sanitizeInput(uiInput.getProjectName());   
			   String tableNames = Sanitization.sanitizeInput(uiInput.getTableNames());  
			   String filterNames = Sanitization.sanitizeInput(uiInput.getFilterNames());	    
			   String toggleValue = uiInput.getToggleValue();			   
			   String upstreamValue = uiInput.getUpstreamValue();
			   String downstreamValue = uiInput.getDownstreamValue();
			    
			if (uiInput.getType().equalsIgnoreCase("node") || uiInput.getType().equalsIgnoreCase("script")) {			   
			   processBuilder = new ProcessBuilder("python3.9",
					   commonScriptLocation + "/" + pyvisLibraryScriptNameNodeOrScript, csrfToken, projectName,
						filterNames, "--thd", toggleValue, "--usl",
						upstreamValue, "--dsl", downstreamValue);			
			} else {			
				if(uiInput.getTableNames().trim().equals("")) {
					 processBuilder = new ProcessBuilder("python3.9",
							 commonScriptLocation + "/" + pyvisLibraryScriptNameTech, csrfToken, projectName, filterNames,
							"--thd", toggleValue);
				} else {
					 processBuilder = new ProcessBuilder("python3.9",
							 commonScriptLocation + "/" + pyvisLibraryScriptNameTech, csrfToken, projectName, filterNames,
							"--thd", toggleValue, "--scripts", tableNames);
				}
				
			}
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				System.out.println("pyvisLibrary script executed successfully");
			} else {
				System.out.println("pyvisLibrary script failed to execute...with exitCode " + exitCode);
			}
			
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in executePyvisLibraryScript Service "+ ex.getMessage());
		}
		return sb.toString();
	}

	public String executePythonScriptForExcelGeneration(UIInput4 uiInput) {
		StringBuilder sb = new StringBuilder();
		try {
			String projectName =  Sanitization.sanitizeInput(uiInput.getProjectName());

			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", excelGenerationScriptLocation, csrfToken, projectName);
			LOGGER.info("python3.9 " + excelGenerationScriptLocation + " " + csrfToken + " " + projectName);
			Process process = processBuilder.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				System.out.println("ExcelGeneration script executed successfully");
			} else {
				System.out.println("ExcelGeneration script failed to execute...with exitCode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in executePythonScriptForExcelGeneration Service "+ ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in executePythonScriptForExcelGeneration Service "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
		return technicalLineageReportFileLocation.concat(uiInput.getProjectName()).concat("_streamed_paths.csv");
	}

	public String[] executePythonScriptForDisplayTableLineage(UIInput3 uiInput) {
		StringBuilder output = new StringBuilder();
		try {
			String projectName = Sanitization.sanitizeInput(uiInput.getProjectName());
			String filterNames = Sanitization.sanitizeInput(uiInput.getFilterNames());
			String type = Sanitization.sanitizeInput(uiInput.getType());
			String tableNames = uiInput.getTableNames();
			ProcessBuilder processBuilder = null;

			if (Constants.TECH.equalsIgnoreCase(type) && StringUtils.isNotEmpty(filterNames)
					&& StringUtils.isNotEmpty(tableNames)) {
				processBuilder = new ProcessBuilder(Constants.PYTHON3_9, pyvisDisplayTableLineageScript, csrfToken,
						projectName, Constants.__TECH, filterNames, Constants.__SCRIPTS, tableNames);
			} else if ((Constants.NODE.equalsIgnoreCase(type) || Constants.SCRIPT.equalsIgnoreCase(type))
					&& !filterNames.isEmpty()) {
				processBuilder = new ProcessBuilder(Constants.PYTHON3_9, pyvisDisplayTableLineageScript, csrfToken,
						projectName, Constants.__TABLE_NAME, filterNames);
			} else if (Constants.TECH.equalsIgnoreCase(type) && !filterNames.isEmpty()) {
				processBuilder = new ProcessBuilder(Constants.PYTHON3_9, pyvisDisplayTableLineageScript, csrfToken,
						projectName, Constants.__TECH, filterNames);
			}

			if (Objects.isNull(processBuilder)) {
				return new String[]{Constants.FAILED, Constants.DISPLAY_LINEAGE_REQUIRED_CRITERIA_ERROR};
			}

			StringBuilder command = new StringBuilder();
			for (String commandItem : processBuilder.command()) {
				command.append(commandItem).append(" ");
			}
			LOGGER.info("Command: {}", command);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);//.append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("pyvis display table lineage script executed successfully");
				return new String[]{Constants.SUCCESS, output.toString()};
			} else {
				LOGGER.info("pyvis display table lineage script failed to execute with exitCode: {} :: " +
						"with following error: {}", exitCode, output);
				return new String[]{Constants.FAILED, output.toString()};
			}
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in executePythonScriptForDisplayTableLineage Service "+ ex1.getMessage());
			return new String[]{Constants.FAILED, ex1.getMessage()};
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in executePythonScriptForDisplayTableLineage Service "+ ex2.getMessage());
			Thread.currentThread().interrupt();
			return new String[]{Constants.FAILED, ex2.getMessage()};
		}
	}
	
	public String executePythonScript(PythonScriptInput pythonScriptInput) {
		StringBuilder sb = new StringBuilder();
		try {
			String projectName = Sanitization.sanitizeInput(pythonScriptInput.getProjectName());	
			String nodeName = Sanitization.sanitizeInput(pythonScriptInput.getNodeName());
			String direction = Sanitization.sanitizeInput(pythonScriptInput.getDirection());
			String pathLimit = String.valueOf(pythonScriptInput.getPathLimit());
			String reportType = Sanitization.sanitizeInput(pythonScriptInput.getReportType());
	    	
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", pythonScriptLocation, csrfToken,
					projectName, nodeName, "--path_limit", pathLimit, "--direction", direction, "--file_format", reportType);
			processBuilder.redirectErrorStream(true);
			StringBuilder command = new StringBuilder();
			for (String commandItem : processBuilder.command()) {
				command.append(commandItem).append(" ");
			}
			LOGGER.info("Command: {}", command);

			Process process = processBuilder.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("Python script executed successfully");
			} else {
				LOGGER.info("Python script execution failed with exit code other than 0.");
			}
			LOGGER.info("Script output: {}", sb);
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in executePythonScript Service "+ ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in executePythonScript Service "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
		return sb.toString();
	}

	public String[] executeKGraphScript(UIInput3 uiInput) {
		StringBuilder output = new StringBuilder();
		try {
			String projectName = Sanitization.sanitizeInput(uiInput.getProjectName());
			String tableNames = Sanitization.sanitizeInput(uiInput.getTableNames());
			String filterNames = Sanitization.sanitizeInput(uiInput.getFilterNames());
			String type = Sanitization.sanitizeInput(uiInput.getType());

			ProcessBuilder processBuilder = null;

			if (Constants.TECH.equalsIgnoreCase(type) && StringUtils.isNotEmpty(filterNames) &&
					StringUtils.isNotEmpty(tableNames)) {
				processBuilder = new ProcessBuilder(Constants.PYTHON3_9, knowledgeGraphScript, csrfToken,
						projectName, Constants.__TECH, filterNames, Constants.__SCRIPTS, tableNames);
			} else if ((Constants.NODE.equalsIgnoreCase(type) || Constants.SCRIPT.equalsIgnoreCase(type))
					&& !filterNames.isEmpty()) {
				processBuilder = new ProcessBuilder(Constants.PYTHON3_9, knowledgeGraphScript, csrfToken,
						projectName, Constants.__TABLE_NAME, filterNames);
			} else if (Constants.TECH.equalsIgnoreCase(type) && !filterNames.isEmpty()) {
				processBuilder = new ProcessBuilder(Constants.PYTHON3_9, knowledgeGraphScript, csrfToken,
						projectName, Constants.__TECH, filterNames);
			}

			if (Objects.isNull(processBuilder)) {
				return new String[]{Constants.FAILED, Constants.DISPLAY_LINEAGE_REQUIRED_CRITERIA_ERROR};
			}

			StringBuilder command = new StringBuilder();
			for (String commandItem : processBuilder.command()) {
				command.append(commandItem).append(" ");
			}
			LOGGER.info("Command: {}", command);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);//.append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("knowledge graph script executed successfully");
				return new String[]{Constants.SUCCESS, output.toString()};
			} else {
				LOGGER.info("knowledge graph script failed to execute with exitCode: {} :: " +
						"with following error: {}", exitCode, output);
				return new String[]{Constants.FAILED, output.toString()};
			}
		} catch (IOException ex1) {
			LOGGER.info("Exception occurred in executePyvisScript Service: {}", ex1.getMessage());
			return new String[]{Constants.FAILED, ex1.getMessage()};
		} catch (InterruptedException ex2) {
			LOGGER.info("Exception occurred in executePyvisScript Service: {}", ex2.getMessage());
			Thread.currentThread().interrupt();
			return new String[]{Constants.FAILED, ex2.getMessage()};
		}
	}
}