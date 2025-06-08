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
public class ExecuteScriptForQlikView {

	@Value("${bteqScriptLocation}")
	private String scriptLocation;

	@Value("${bteqScriptName}")
	private String scriptName;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${qlikViewScript}")
	private String qvScript;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Autowired
	LineageJobRepository jobRepo;

	@Autowired
	ScriptComplexity scriptComplexity;

	@Autowired
	ScriptLineageService scriptLineageService;

	//bteqExecutionId is job id
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteScriptForQlikView.class);

	@Async
	public void executeFirstScript(String inputFileLocation, String outputFileLocation, 
		String logFileDirectory, String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {
		
		LOGGER.info("bteqExecutionId . . . " + bteqExecutionId);
		// process builder to execute script
		LOGGER.info("Bteq processing start (QLIKVIEW) . . .");
		
		inputFileLocation = Sanitization.sanitizeInput(inputFileLocation);
		outputFileLocation = Sanitization.sanitizeInput(outputFileLocation);
		logFileDirectory = Sanitization.sanitizeInput(logFileDirectory);
	    technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);

		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(bteqExecutionId),
				inputFileLocation, TechnologyConstants.TECH_PARAMS.get(technology), technology);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
			return;
		}

		ProcessBuilder processBuilder = new ProcessBuilder();
		LOGGER.info("bash " + scriptLocation + "/" + qvScript + " " + inputFileLocation + "/ " + outputFileLocation
				+ "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId);
		processBuilder.command("bash", "-c", scriptLocation + "/" + qvScript + " " + inputFileLocation + "/ "
				+ outputFileLocation + "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId);

		Process process = processBuilder.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		
		while ((line = errorReader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		
		LOGGER.info("Bteq script execution output (QLIKVIEW) . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Bteq bash script executed successfully (QLIKVIEW)");
			executePythonBteqScript(bteqExecutionId, technology, projectName);
		} else {
			LOGGER.info("Error in executing Bteq bash script (QLIKVIEW)");
		}
		
		LineageJob lineageJob = jobRepo.findById(Long.valueOf(bteqExecutionId)).get();
		lineageJob.setEndTime(new Date());
		lineageJob.setUploadDir(inputFileLocation);
		jobRepo.save(lineageJob);

		scriptComplexity.calculateScriptComplexity(projectName, Integer.parseInt(bteqExecutionId), technology,
				lineageJob.getJobParams());

		LOGGER.info("Bteq processing end for bash scripts (QLIKVIEW) . . .");

	}
	// End of script execution method
	
	
	
	public void executePythonBteqScript(String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {

		// process builder to execute script
		LOGGER.info("Bteq python processing start (QLIKVIEW) . . .");
		
	    technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);
		
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + commonScriptName +" "+csrfToken+" "+bteqExecutionId +" "+technology+" "+projectName);

		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName, csrfToken, bteqExecutionId, projectName, technology);
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		LOGGER.info("Bteq python script execution output (QLIKVIEW) . . . " + output.toString());
		
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Bteq python script (QLIKVIEW) executed successfully.");
			scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(bteqExecutionId), projectName);
		} else {
			LOGGER.info("Error in executing Bteq python script (QLIKVIEW) . . . RETURN CODE: "+exitCode);
		}

		LOGGER.info("Bteq python script processing end (QLIKVIEW). . .");

	}
	// End of script execution method

	

}
