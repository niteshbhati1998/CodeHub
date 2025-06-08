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
public class ExecuteScriptForKsh {

	@Value("${bteqScriptLocation}")
	private String scriptLocation;
	
	@Value("${kshScriptName}")
	private String kshScriptName;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Autowired
	LineageJobRepository jobRepo;

	@Autowired
	ScriptLineageService scriptLineageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteScriptForKsh.class);

	//for ksh
	//bteqExecutionId is job id
	//object type not needed
	@Async
	public void executeFirstScript(String inputFileLocation, String outputFileLocation, 
			String logFileDirectory, String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {
		
		LOGGER.info(" Ksh processing start . . ." +"<>  ExecutionId . . . " + bteqExecutionId);
		LOGGER.info("python3.9 "+  scriptLocation + "/" + kshScriptName +" "+inputFileLocation +" "+logFileDirectory+" "+csrfToken);
		
		String tech = technology.replaceAll("[^a-zA-Z]", "");
		projectName = projectName.replaceAll("[^a-zA-Z0-9_]", "");

		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(bteqExecutionId),
				inputFileLocation, TechnologyConstants.TECH_PARAMS.get(tech), tech);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
			return;
		}

		ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptLocation + "/" + kshScriptName, inputFileLocation, logFileDirectory, csrfToken, bteqExecutionId );
		Process process = processBuilder.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		LOGGER.info("Bteq script execution output . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Ksh python first script executed successfully");
			executePythonKshScript(bteqExecutionId, tech,projectName);
		} else {
			LOGGER.info("Error in executing Ksh python first script");
		}
		
		LineageJob lineageJob = jobRepo.findById(Long.valueOf(bteqExecutionId)).get();
		lineageJob.setEndTime(new Date());
		lineageJob.setUploadDir(inputFileLocation);
		jobRepo.save(lineageJob);

		LOGGER.info("Ksh processing end for first python scripts. . .");

	}
	// End of python script execution method
	
	public void executePythonKshScript(String bteqExecutionId, String technology, String projectName) throws IOException, InterruptedException {

		// process builder to execute script
		LOGGER.info("Ksh python processing start . . .");
		
		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);
		
		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName, csrfToken, bteqExecutionId,  projectName, technology);
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + commonScriptName +" "+csrfToken+" "+bteqExecutionId +" "+technology+" "+projectName);
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		
		LOGGER.info("Ksh python script execution output . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Ksh python script executed successfully");
			scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(bteqExecutionId), projectName);
		} else {
			LOGGER.info("Error in executing Ksh python script");
		}

		LOGGER.info("Ksh python script processing end . . .");

	}
	// End of python script execution method
	

}
