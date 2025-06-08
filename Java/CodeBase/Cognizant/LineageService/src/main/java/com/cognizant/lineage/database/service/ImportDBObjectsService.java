package com.cognizant.lineage.database.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cognizant.lineage.database.dao.ImportDBObjectsDAO;
import com.cognizant.lineage.database.model.DbScriptDetails;
import com.cognizant.lineage.database.model.Response4;
import com.cognizant.lineage.database.model.UIInput1;
import com.cognizant.lineage.database.model.VaultDataWithAllFields;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.service.ScriptComplexity;
import com.cognizant.lineage.upload.service.ScriptLineageService;
import com.cognizant.lineage.util.Sanitization;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ImportDBObjectsService {
	
	@Value("${csrfToken}")
	private String csrfToken;

	@Value("${vault.url}")
	private String vaultURL;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${ExtractDbObjectForTeradataScriptName}")
	private String extractDbObjectForTeradataScriptName;
	
	@Value("${ExtractDbObjectForTeradataScriptLocation}")
	private String extractDbObjectForTeradataScriptLocation;
	
	@Value("${ExtractDbObjectForOracleScriptName}")
	private String extractDbObjectForOracleScriptName;
	
	@Value("${ExtractDbObjectForMSSQLScriptName}")
	private String extractDbObjectForMSSQLServerScriptName;
	
	@Value("${ExtractDbObjectForOracleScriptLocation}")
	private String extractDbObjectForOracleScriptLocation;
	
	@Value("${ExtractDbObjectForMSSQLServerScriptLocation}")
	private String extractDbObjectForMSSQLServerScriptLocation;
	
	@Value("${bteqScriptLocation}")
	private String scriptLocationAllStepsStartingFromCleansing;
	
    @Value("${bteqOutputFileLocation}")
    private String outputFileLocation;
	  
	@Value("${bteqScriptName}")
	private String scriptNameAllStepsStartingFromCleansing;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${bteqPythonScriptName}")
	private String bteqPythonScriptName;
			
	@Value("${dbobjects.extraction.path}")
	private String dbobjectsExtractionPath;
	
    @Autowired
    ImportDBObjectsDAO importDBObjectsDAO;

	@Autowired
	ScriptComplexity scriptComplexity;

	@Autowired
	ScriptLineageService scriptLineageService;

    //Endpoint: dropdown
	public Set<String> getDatabaseTypeList(Logger LOGGER) {
		Set<String> databaseTypeList = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			String requestBody = "{\"path\": \"application\"}";
			HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody, headers);
	
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(vaultURL, HttpMethod.POST, requestEntity, String.class);

			ObjectMapper objectMapper = new ObjectMapper();
			List<VaultDataWithAllFields> list = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			databaseTypeList = list.stream().map(vaultDataWithAllFields -> vaultDataWithAllFields.getValue().getDbtype()).collect(Collectors.toSet());
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getDatabaseTypeList Service "+ex.getMessage());
		}
		return databaseTypeList;
	}
	
	public List<String> getConnectionNameList(String databaseType, Logger LOGGER) {
		List<String> connectionNameList = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			String requestBody = "{\"path\": \"application\"}";
			HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
	
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(vaultURL, HttpMethod.POST, requestEntity, String.class);

			ObjectMapper objectMapper = new ObjectMapper();
			List<VaultDataWithAllFields> list = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			connectionNameList = list.stream().filter(vaultDataWithAllField -> vaultDataWithAllField.getValue().getDbtype().equals(databaseType)).map(vaultDataWithAllField -> vaultDataWithAllField.getKey()).collect(Collectors.toList());
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getConnectionList Service " + ex.getMessage());
		}
		return connectionNameList;
	}
    
	//Endpoint: ExtractButton
	public void sanitizeInputs(UIInput1 uiInputList, Logger LOGGER) {
		try {
			uiInputList.setProjectName(Sanitization.sanitizeInput(uiInputList.getProjectName()));
			uiInputList.setDatabaseType(uiInputList.getDatabaseType());
			uiInputList.setConnectionName(Sanitization.sanitizeInput(uiInputList.getConnectionName()));
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in sanitizeInputs Service " + ex.getMessage());
		}
	}
	
	public List<Integer> saveDatabaseObjectTypeDetails(UIInput1 uiInput1, Logger LOGGER) {
		List<Integer> jobIdList = new ArrayList<>();
		try {	    
			String projectName = uiInput1.getProjectName();
			String databaseType = uiInput1.getDatabaseType();
			String connectionName = uiInput1.getConnectionName();
			
			if (TechnologyConstants.TERADATA.equalsIgnoreCase(databaseType)) {

				if (uiInput1.isView()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "View", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isProcedure()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Procedure", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isFunction()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Function", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isMacro()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Macro", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
			} else if(TechnologyConstants.ORACLE.equalsIgnoreCase(databaseType)) {
				
				if (uiInput1.isView()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "View", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isProcedure()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Procedure", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isFunction()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Function", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isTrigger()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Trigger", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isPackageName()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Package", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
			} else if (TechnologyConstants.MS_SQL_SERVER.equalsIgnoreCase(databaseType)) {

				if (uiInput1.isView()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "View", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isProcedure()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Procedure", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isFunction()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Function", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
				if (uiInput1.isTrigger()) {
					int jobId = importDBObjectsDAO.getJobId(LOGGER);
					jobIdList.add(jobId);
					importDBObjectsDAO.saveDatabaseObjectTypeDetails(jobId, projectName, databaseType, "Trigger", "D", dbobjectsExtractionPath+"/"+jobId, connectionName, LOGGER);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occurred in saveDatabaseObjectTypeDetails DAO "+ex.getMessage());
		}
		return jobIdList;
	}
	
	@Async
	public void executeExtractFunctionality(List<DbScriptDetails> dbScriptDetailsList, UIInput1 uiInputList, Response4 response, Logger LOGGER) {
		int exitCode;
		try {
			for (DbScriptDetails dbScriptDetails : dbScriptDetailsList) {
				int jobId = dbScriptDetails.getJobId();
				String objectType = dbScriptDetails.getDatabaseObjectType();
				String inputLocation = dbScriptDetails.getLocation();
				String databaseType = dbScriptDetails.getDatabaseType();

				LOGGER.info("Step2: start calling 1st python script for extracting db-objects from "+ uiInputList.getDatabaseType() +" for jobId "+jobId + " and object_type "+objectType);
				if (!GeneralConstants.UPLOAD_TYPE.equalsIgnoreCase(dbScriptDetails.getUploadType())) {
			     exitCode = captureDbObjects(String.valueOf(jobId), uiInputList.getDatabaseType(), LOGGER);
				} else {
					int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing((long) jobId,
							dbScriptDetails.getLocation(), dbScriptDetails.getDatabaseType(),
							dbScriptDetails.getDatabaseObjectType());
					if (exitCodeForScriptLineage != 0) {
						LOGGER.info("Script Lineage exited with exit code: " + exitCodeForScriptLineage);
						return;
					}
					exitCode = 0;
				}

				if (exitCode == 0) {
					LOGGER.info("Step2: db-objects extracted successfully from " + uiInputList.getDatabaseType());
					LOGGER.info("Step3: start calling 2nd python script for performing all Steps starting from cleansing");

					File newInputDirectory = new File(inputLocation);
					String outputDir = Sanitization.sanitizeInput(outputFileLocation + File.separator + jobId);
					File newOutputDirectory = new File(outputDir);
                    newOutputDirectory.mkdir();
					LOGGER.info(".......inputLocation: " + newInputDirectory.getAbsolutePath());
					LOGGER.info(".......outputLocation: " + newOutputDirectory.getAbsolutePath());

					executePythonScriptStartingFromCleansing(newInputDirectory.getAbsolutePath(),newOutputDirectory.getAbsolutePath(),
							logFileLocation, jobId, databaseType, uiInputList.getProjectName(), objectType, LOGGER);

					scriptComplexity.calculateScriptComplexity(dbScriptDetails.getProjectName(), dbScriptDetails.getJobId(),
							dbScriptDetails.getDatabaseObjectType(), dbScriptDetails.getDatabaseType());

					LOGGER.info("Step3: All Steps starting from cleansing performed successfully");
					response.setMessage("Extraction and all LineageSteps completed successfully");
				} else {
					LOGGER.info("Step2:Extraction script failed with exit code " + exitCode);
					response.setMessage("Extraction script failed with exit code " + exitCode);
				}
				if (GeneralConstants.UPLOAD_TYPE.equalsIgnoreCase(dbScriptDetails.getUploadType())) {
					scriptLineageService.invokeScriptLineageIdentification((long) jobId, dbScriptDetails.getProjectName());
				}
			}
		} catch (IOException | LineageBusinessException ex1) {
			LOGGER.info("Exception occurred in executeExtractFunctionality Service " + ex1.getMessage());
			response.setMessage("Exception occurred in executeExtractFunctionality Service");
		} catch (InterruptedException ex2) {
			LOGGER.info("Exception occurred in executeExtractFunctionality Service " + ex2.getMessage());
			response.setMessage("Exception occurred in executeExtractFunctionality Service");
			Thread.currentThread().interrupt();
		}
	}
	
	//extraction script
	public int captureDbObjects(String jobId, String databaseType, Logger LOGGER) throws LineageBusinessException {
		int exitCode=0;
		ProcessBuilder processBuilder = new ProcessBuilder();
		try {
			if(databaseType.equalsIgnoreCase("TERADATA")) {
			    processBuilder = new ProcessBuilder("python3.9",extractDbObjectForTeradataScriptLocation + "/" + extractDbObjectForTeradataScriptName, "-job_id", jobId);
				LOGGER.info(".........JOBID: "+jobId);
				LOGGER.info(".........TERADATA scriptName: "+extractDbObjectForTeradataScriptName);
				LOGGER.info(".........TERADATA scriptLocation: "+extractDbObjectForTeradataScriptLocation);
			} else if(databaseType.equalsIgnoreCase("ORACLE")) {
			    processBuilder = new ProcessBuilder("python3.9",extractDbObjectForOracleScriptLocation + "/" + extractDbObjectForOracleScriptName, "-job_id", jobId);
				LOGGER.info(".........JOBID: "+jobId);
				LOGGER.info(".........ORACLE scriptName: "+extractDbObjectForOracleScriptName);
				LOGGER.info(".........ORACLE scriptLocation: "+extractDbObjectForOracleScriptLocation);
			} else if(databaseType.equalsIgnoreCase("MS SQL Server")) {
			    processBuilder = new ProcessBuilder("python3.9",extractDbObjectForMSSQLServerScriptLocation + "/" + extractDbObjectForMSSQLServerScriptName, jobId);
				LOGGER.info(".........JOBID: "+jobId);
				LOGGER.info(".........MS SQL Server scriptName: "+extractDbObjectForMSSQLServerScriptName);
				LOGGER.info(".........MS SQL Server scriptLocation: "+extractDbObjectForMSSQLServerScriptLocation);
			}
			Process process = processBuilder.start();
			
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info(".........received exitcode 0");
			} else {
				LOGGER.info(".........received exitcode "+exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in captureDbObjects Service " + ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in captureDbObjects Service " + ex2.getMessage());
			Thread.currentThread().interrupt();
		}
		return exitCode;
	}
	
	//cleansingScript
	public void executePythonScriptStartingFromCleansing(String inputFileLocation, String outputFileLocation,
														 String logFileDirectory, int jobId,
														 String databaseType, String projectName,
														 String objectType, Logger LOGGER) throws IOException, InterruptedException {
		
		String technology = "";
		if(TechnologyConstants.ORACLE.equalsIgnoreCase(databaseType)) {
			technology = databaseType.toLowerCase() + objectType.toLowerCase();
		} else if(TechnologyConstants.TERADATA.equalsIgnoreCase(databaseType)) {
			if(objectType.equalsIgnoreCase("StoredProcedure")) {
				technology = "procedure";
			} else {
				technology = objectType.toLowerCase();
			}
		} else if(TechnologyConstants.MS_SQL_SERVER.equalsIgnoreCase(databaseType)) {
			technology = databaseType.replaceAll("\\s","").toLowerCase() + objectType.toLowerCase();
		}
		
		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);
		inputFileLocation = Sanitization.sanitizeInput(inputFileLocation);
		outputFileLocation = Sanitization.sanitizeInput(outputFileLocation);
		logFileDirectory = Sanitization.sanitizeInput(logFileDirectory);

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("bash", scriptLocationAllStepsStartingFromCleansing + "/" + scriptNameAllStepsStartingFromCleansing , inputFileLocation, outputFileLocation, logFileDirectory, csrfToken, technology, String.valueOf(jobId));
		LOGGER.info("bash " + scriptLocationAllStepsStartingFromCleansing + "/" + scriptNameAllStepsStartingFromCleansing + " " +inputFileLocation + " " + outputFileLocation + " " +logFileDirectory+ " " +csrfToken+ " " +technology+ " " +String.valueOf(jobId));
		Process process = processBuilder.start();
		LOGGER.info(".........2nd pythonscript JobId: "+jobId);
		LOGGER.info(".........2nd pythonscript Name: "+scriptNameAllStepsStartingFromCleansing);
		LOGGER.info(".........2nd pythonscript Location: "+scriptLocationAllStepsStartingFromCleansing);
		
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		
		int exitCode = process.waitFor();
		if (exitCode == 0) {
			LOGGER.info(".........received exitcode 0");
			LOGGER.info(".........Step3(1): start calling 3rd pythonscript for performing last step");
			executeScriptForLastStep(jobId, technology, projectName, LOGGER);
			LOGGER.info(".........Step3(1): last step script executed successfully");
		} else {
			LOGGER.info(".........received exitcode "+exitCode);
		}
	}
	
	//laststep script
	public void executeScriptForLastStep(int jobId, String technology, String projectName, Logger LOGGER) throws IOException, InterruptedException {

		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);

		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + bteqPythonScriptName, csrfToken, String.valueOf(jobId),  projectName, technology);
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "+csrfToken +" "+jobId +" "+projectName+" "+technology);
		Process process = processBuilder2.start();
		LOGGER.info(".........jobId: "+jobId);
		LOGGER.info(".........3rd pythonscript Name: "+bteqPythonScriptName);
		LOGGER.info(".........3rd pythonscript Location: "+commonScriptLocation);
		LOGGER.info(".........sending technology to config file: "+technology);
		
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}

		int exitCode = process.waitFor();
		if (exitCode == 0) {
			LOGGER.info(".........received exitcode 0");
		} else {
			LOGGER.info(".........received exitcode "+exitCode);
		}
	}
	
	//Endpoint: StatusButton
	public String getDbObjectStatus(int jobId) {
		String status = "";
		try {
			int completedCount = importDBObjectsDAO.getDbObjectStatus(jobId, "COMPLETED");
			if(completedCount==13) {
				status = "Success";
			} else {
				int errorCount = importDBObjectsDAO.getDbObjectStatus(jobId, "ERROR");
				if(errorCount==0) {
					if(completedCount>0) {
						status = "Inprocess";
					} else {
						int rowCount = importDBObjectsDAO.getDbObjectStatus(jobId, "");
						if(rowCount==0) {
							status = "Not Started";
						} else {
							status = "Inprocess";
						}
					}
				} else {
					status =  "Error";
				}
			}
	    } catch(Exception ex) {
		    System.out.println("Exception occurred in getDbObjectStatus Service "+ex.getMessage());
	    }
		return status;
	}
}
