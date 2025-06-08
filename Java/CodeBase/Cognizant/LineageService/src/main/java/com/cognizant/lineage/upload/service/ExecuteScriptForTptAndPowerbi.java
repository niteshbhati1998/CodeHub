package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.util.Sanitization;

@Service
public class ExecuteScriptForTptAndPowerbi {

	@Value("${bteqScriptLocation}")
	private String scriptLocation;
	
	@Value("${bteqPythonScriptLocation}")
	private String bteqPythonScriptLocation;

	@Value("${bteqScriptName}")
	private String scriptName;
	
	@Value("${bteqPythonScriptName}")
	private String bteqPythonScriptName;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${tptStepZeroScripName}")
	private String tptScriptName;
	
	@Value("${powerbiStepZeroScriptName}")
	private String powerbiScriptName;
	
	@Value("${ouputDirForTptAndPowerbi}")
	private String ouputDirForTptAndPowerbi;
	
	@Value("${pythonScriptForUploadFunctionality}")
	private String pythonScriptForUploadFunctionality;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${outputLocationForLineageShellScript}")
	private String outputLocationForLineageShellScript;
	
	@Autowired
	LineageJobRepository jobRepo;

	@Autowired
	ScriptComplexity scriptComplexity;

	@Autowired
	ScriptLineageService scriptLineageService;

	// bteqExecutionId id job_id
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteScriptForTptAndPowerbi.class);

	@Async
	public void executeZeroScript(String inputFileLocation, String outputFileLocation, 
			String logFileDirectory, String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {
		
		String script = "";
		LOGGER.info("Job Id . . . " + bteqExecutionId);

		String tech = technology.replaceAll("[^a-zA-Z]", "");
		projectName = projectName.replaceAll("[^a-zA-Z0-9_]", "");

		if(tech.equalsIgnoreCase("tpt")) {
			script = tptScriptName;
		} else {
			script = powerbiScriptName;
		}

		if(tech.equalsIgnoreCase("tpt")) {
			int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(bteqExecutionId),
					inputFileLocation, TechnologyConstants.TECH_PARAMS.get(tech), tech);
			if (exitCodeForScriptLineage != 0) {
				LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
				return;
			}
		}
		// process builder to execute script
		LOGGER.info("Processing start (TPT & PowerBI) . . .");

		StringBuilder command = new StringBuilder();
		ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptLocation + "/" + script,  inputFileLocation,
				ouputDirForTptAndPowerbi, logFileDirectory, csrfToken, bteqExecutionId);
		for (String commandItem : processBuilder.command()) {
			command.append(commandItem).append(" ");
		}
		LOGGER.info("Script Lineage Cleansing Command: {}", command);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();
		LOGGER.info("tpt cleansing script execution output for tpt and power bi . . . " + output);
		if (exitCode == 0) {
			LOGGER.info("tpt pre cleansing python script executed successfully For tpt and power bi");
//			if (!tech.equalsIgnoreCase("tpt")){
//				int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(bteqExecutionId),
//						inputFileLocation, TechnologyConstants.TECH_PARAMS.get(tech), tech);
//				if (exitCodeForScriptLineage != 0) {
//					LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
//					return;
//				}
//			}
			executeFirstScript(ouputDirForTptAndPowerbi, outputFileLocation, logFileDirectory, bteqExecutionId, tech, projectName);
		} else {
			LOGGER.info("Error in executing Bteq pre cleansing python script . . . with RETURN CODES = "+exitCode);
		}
		
		LineageJob lineageJob = jobRepo.findById(Long.valueOf(bteqExecutionId)).get();
		lineageJob.setEndTime(new Date());
		lineageJob.setUploadDir(inputFileLocation);
		jobRepo.save(lineageJob);

		scriptComplexity.calculateScriptComplexity(projectName, Integer.parseInt(bteqExecutionId), tech,
				lineageJob.getJobParams());

		LOGGER.info("Bteq processing end for python, bash and python scripts. . .");
	}
	
	@Async
	public void executePythonScript(String inputFileLocation, String outputFileLocation, String logFileLocation, String jobId, String technology, String projectName) throws Exception {
       	projectName = Sanitization.sanitizeInput(projectName);
		technology = Sanitization.sanitizeInput(technology);

		ProcessBuilder processBuilder = new ProcessBuilder("python3.9", pythonScriptForUploadFunctionality, csrfToken, inputFileLocation, outputFileLocation, logFileLocation, jobId, "queries");
		LOGGER.info("python3.9 "+ pythonScriptForUploadFunctionality +" "+ csrfToken +" "+ inputFileLocation +" "+ outputFileLocation +" "+ logFileLocation +" "+ jobId +" " +"queries");
		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		LOGGER.info("Python script for upload functionality output: " + output.toString());

		int exitCode = process.waitFor();
		if (exitCode == 0) {
			executeFirstScript(outputFileLocation, outputLocationForLineageShellScript+"/"+jobId, logFileLocation, jobId, technology, projectName);
			LOGGER.info("Python script for upload functionality executed successfully");
		} else {
			LOGGER.info("Error in executing python script for upload functionality");
		}
		LineageJob lineageJob = jobRepo.findById(Long.valueOf(jobId)).get();
		lineageJob.setEndTime(new Date());
		lineageJob.setUploadDir(inputFileLocation);
		jobRepo.save(lineageJob);
	}
	
	public void executeFirstScript(String inputFileLocation, String outputFileLocation, 
			String logFileDirectory, String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {
		
		LOGGER.info("Job ID . . . " + bteqExecutionId);
		// process builder to execute script
		LOGGER.info("Bteq processing start . . .");
		
		inputFileLocation = Sanitization.sanitizeInput(inputFileLocation);
		outputFileLocation = Sanitization.sanitizeInput(outputFileLocation);
		logFileDirectory = Sanitization.sanitizeInput(logFileDirectory);
		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("bash", "-c", scriptLocation + "/" + scriptName + " " + inputFileLocation + "/ "
				+ outputFileLocation + "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId);
		LOGGER.info("bash " + scriptLocation + "/" + scriptName + " " + inputFileLocation + "/ " + outputFileLocation
				+ "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId);
		Process process = processBuilder.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		LOGGER.info("Bteq script execution output . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Bteq bash script executed successfully");
			executePythonBteqScript(bteqExecutionId, technology, projectName);
		} else {
			LOGGER.info("Error in executing Bteq bash hscript");
		}

		LOGGER.info("Bteq processing end for bash and python scripts. . .");

	}
	// End of script execution method
	
	
	
	public void executePythonBteqScript(String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {

		// process builder to execute script
		LOGGER.info("Bteq python processing start . . .");
		
		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);
		
		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + bteqPythonScriptName, csrfToken, bteqExecutionId, projectName, technology);
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "+csrfToken+" "+bteqExecutionId +" "+projectName+" "+technology);
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		LOGGER.info("Bteq python script execution output . . . " + output.toString());
		
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Bteq python script executed successfully.");
			if ("tpt".equalsIgnoreCase(technology)) {
				scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(bteqExecutionId), projectName);
			}
		} else {
			LOGGER.info("Error in executing Bteq python script . . . RETURN CODE: "+exitCode);
		}

		LOGGER.info("Bteq python script processing end . . .");

	}
	// End of script execution method

	

}