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
public class ExecuteScript {

	@Value("${mloadScriptName}")
	private String mloadScriptName;

	@Value("${mloadScriptLocation}")
	private String mloadScriptLocation;
			
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
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Autowired
	LineageJobRepository jobRepo;

	@Autowired
	ScriptComplexity scriptComplexity;

	@Autowired
	ScriptLineageService scriptLineageService;

	/** Nitesh check this part **********
	@Autowired
	ColumnLineageService columnLineageService;
	**/

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteScript.class);

	@Async
	public void executeFirstScript(String inputFileLocation, String outputFileLocation, 
			String logFileDirectory, String bteqExecutionId, String technology, String projectName) throws Exception {
		
		LOGGER.info("Job Id . . . " + bteqExecutionId);

		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);

		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(bteqExecutionId),
				inputFileLocation, TechnologyConstants.TECH_PARAMS.get(technology), technology);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
			return;
		}

		LOGGER.info("Bteq processing start . . .");
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("bash", "-c", scriptLocation + "/" + scriptName + " " + inputFileLocation + "/ "
				+ outputFileLocation + "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId	);
		LOGGER.info("bash -c " + scriptLocation + "/" + scriptName + " " + inputFileLocation + "/ "
				+ outputFileLocation + "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId);
//		System.out.println("bash"+" "+scriptLocation + "/" + scriptName + " " + inputFileLocation + "/ "
//				+ outputFileLocation + "/ " + logFileDirectory + "/ "+csrfToken+" "+technology+" "+bteqExecutionId);
		Process process = processBuilder.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		
		while ((line = errorReader.readLine()) != null) {
			output.append(line).append("\n");
		}
		LOGGER.info("Bteq script execution output . . . " + output.toString());
//		System.out.println(output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCodeForParsingScript = process.waitFor();
		if (exitCodeForParsingScript == 0) {
			LOGGER.info("Bteq bash script executed successfully");
			executePythonBteqScript(bteqExecutionId, technology, projectName);
		} else {
			LOGGER.info("Error in executing Bteq bash script");
		}

		LineageJob lineageJob = jobRepo.findById(Long.valueOf(bteqExecutionId)).get();
		lineageJob.setEndTime(new Date());
		lineageJob.setUploadDir(inputFileLocation);
		jobRepo.save(lineageJob);
		scriptComplexity.calculateScriptComplexity(projectName, Integer.parseInt(bteqExecutionId), technology,
				lineageJob.getJobParams());
	}
	// End of script execution method
	
	public int executePythonBteqScript(String bteqExecutionId, String technology, String projectName) throws Exception {

		// process builder to execute script
		LOGGER.info("Bteq python processing start . . .");
		
		projectName = Sanitization.sanitizeInput(projectName);
		technology = Sanitization.sanitizeInput(technology);
		
		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + bteqPythonScriptName, csrfToken, bteqExecutionId,  projectName, technology);
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "+csrfToken +" "+bteqExecutionId +" "+projectName+" "+technology);
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

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Bteq python script executed successfully");
			scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(bteqExecutionId), projectName);
		} else {
			LOGGER.info("Error in executing Bteq python script");
		}
		return exitCode;
	}
	// End of script execution method
	
	
	public void executePythonInformaticaScript(String infaExecutionId, String projectName) throws IOException, InterruptedException {

		// process builder to execute script
		LOGGER.info("Informatica python processing start . . .");
		
		projectName = Sanitization.sanitizeInput(projectName);
		
		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + bteqPythonScriptName, csrfToken, infaExecutionId, projectName, "infa" );
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "+csrfToken+" "+infaExecutionId +" "+projectName+" infa");
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		LOGGER.info("Informatica python script execution output . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Informatica python script executed successfully.");
			scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(infaExecutionId), projectName);
		} else {
			LOGGER.info("Error in executing Informatica python script");
		}

		LOGGER.info("Informatica python script processing end . . .");

	}
		
	@Async
	public void executeMLoadScript(String inputFileLocation, String jobId, String technology, String projectName, String logFileLocation) throws Exception {
		LOGGER.info("MLoad script processing start . . .");
		
		projectName = Sanitization.sanitizeInput(projectName);
		technology = Sanitization.sanitizeInput(technology);

		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(jobId),
				inputFileLocation, TechnologyConstants.TECH_PARAMS.get(technology), technology);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
			return;
		}
		
		ProcessBuilder processBuilder = new ProcessBuilder("python3.9", mloadScriptLocation + "/" + mloadScriptName, "--input", inputFileLocation, "--job_id", jobId, "--project_name", projectName, "--log_file_location", logFileLocation);
		LOGGER.info("python3.9 "+ mloadScriptLocation +"/"+ mloadScriptName +" "+ "--input" +" "+ inputFileLocation +" "+ "--job_id" +" "+ jobId +" "+ "--project_name" +" "+ projectName +" "+ "--log_file_location" +" "+ logFileLocation);
		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		LOGGER.info("MLoad python script execution output . . . " + output.toString());

		int exitCode = process.waitFor();
		if (exitCode == 0) {
			executePythonBteqScript(jobId, technology, projectName);
			LOGGER.info("MLoad python script executed successfully");
		} else {
			LOGGER.info("Error in executing MLoad python script");
		}
		LineageJob lineageJob = jobRepo.findById(Long.valueOf(jobId)).get();
		lineageJob.setEndTime(new Date());
		lineageJob.setUploadDir(inputFileLocation);
		jobRepo.save(lineageJob);
		scriptComplexity.calculateScriptComplexity(projectName, Integer.parseInt(jobId),
				technology, lineageJob.getJobParams());
	}
}
