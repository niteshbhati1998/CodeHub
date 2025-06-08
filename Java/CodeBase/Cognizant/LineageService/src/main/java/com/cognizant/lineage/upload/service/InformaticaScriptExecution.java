package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.dao.InformaticaLineageDao;
import com.cognizant.lineage.util.Sanitization;

@Service
public class InformaticaScriptExecution {
	
	@Value("${bteqScriptLocation}")
	private String scriptLocation;
	
	@Value("${bteqScriptName}")
	private String scriptName;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${infaFileScript}")
	private String infaFileScript;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${bteqInputFiles}")
	private String bteqUploadFileLocation;
	
	@Value("${bteqOutputFileLocation}")
	private String outputFileLocation;
	
	@Value("${logFileLocation}")
	private String logFileDirectory;
	
	private String fileCreationLocation;
	private String outputDirectory;
	private File newInputDirectory;
	private String sequenceId;
	private File newOutputDirectory;
	
	@Autowired
	InformaticaLineageDao infaDao;
	
	//sequenceId = bteqExecutionId = job_id - all are same
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InformaticaScriptExecution.class);
	
	public void preProcessingPreparations(int sequenceId) {
		this.sequenceId = String.valueOf(sequenceId);
		this.newInputDirectory = new File(bteqUploadFileLocation+File.separator+sequenceId);
		newInputDirectory.mkdir();
		this.fileCreationLocation = this.newInputDirectory.getAbsolutePath();
		
		this.newOutputDirectory = new File(outputFileLocation+File.separator+sequenceId);
		newOutputDirectory.mkdir();
		this.outputDirectory = newOutputDirectory.getAbsolutePath();
	}
	
	
	public String executeFileCreationScript(String technology, String projectName, int sequenceId) throws Exception {
		
		preProcessingPreparations(sequenceId);
		
		String result = "";
		// process builder to execute script
		LOGGER.info("Informatica python script for file creation start . . ."+this.sequenceId);
		
		//String tech = technology.replaceAll("[^a-zA-Z]", "");
		projectName = projectName.replaceAll("[^a-zA-Z0-9_]", "");
		
		LOGGER.info("python3.9 "+ scriptLocation + "/" + infaFileScript +" "+this.fileCreationLocation 
				+" "+this.logFileDirectory +" "+this.csrfToken+" "+	this.sequenceId);
		ProcessBuilder processBuilder2 = new ProcessBuilder
		("python3.9", scriptLocation + "/" + infaFileScript, 
				this.fileCreationLocation, this.logFileDirectory, this.csrfToken, this.sequenceId );
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		LOGGER.info("Informatica python script for file creation . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		try {
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				//call method to execute bteq bash script,
				executeFirstScript(this.fileCreationLocation, this.outputDirectory, this.logFileDirectory, this.sequenceId,
						technology,projectName );
			}
		} catch (InterruptedException e) {
			LOGGER.info("Failed to execute bash script for informatica");
			Thread.currentThread().interrupt();
		}
		
		return result;
	}
	
	
	
	////
	public void executeFirstScript(String inputFileLocation, String outputFileLocation, 
			String logFileDirectory, String bteqExecutionId, String technology, String projectName) throws Exception {
		
		LOGGER.info("Informatica Job Id . . . " + bteqExecutionId);
		LOGGER.info("Informatica processing start . . .");
		
		String tech = technology.replaceAll("[^a-zA-Z]", "");
		projectName = projectName.replaceAll("[^a-zA-Z0-9_]", "");
		
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
			output.append(line).append("\n");
		}
		LOGGER.info("Informatica lineage bash script execution output . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Informatica bash script executed successfully");
			executePythonBteqScript(bteqExecutionId, tech,projectName);
		} else {
			LOGGER.info("Error in executing Informatica bash hscript");
		}

		LOGGER.info("Informatica processing end for bash and python scripts. . .");

	}
	// End of script execution method
	
	public void executePythonBteqScript(String bteqExecutionId, String technology, String projectName) throws Exception {

		// process builder to execute script
		LOGGER.info("Informatica python processing start . . .");
		
		technology = Sanitization.sanitizeInput(technology);
		projectName = Sanitization.sanitizeInput(projectName);
		
		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName, csrfToken, bteqExecutionId,  projectName, technology );
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + commonScriptName +" "+csrfToken +" "+bteqExecutionId +" "+projectName+" "+technology);
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			output.append(line).append("\n");
		}
		LOGGER.info("Informatica  python script execution output . . . " + output.toString());

		// Wait for the script to finish executing and get the exit code
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Informatica  python script executed successfully");
		} else {
			LOGGER.info("Error in executing Informatica python script");
		}

		LOGGER.info("Informatica python script processing end . . .");

	}
	// End of script execution method
	

	
	public boolean infaSqlTableStatus(int job_id) {
		boolean flag=false;
		
		int rowCount = infaDao.infaSqlTableRowCountStatus(job_id);
		
		if(rowCount > 0 ) {
			flag = true;
		}
		
		return flag;
	}
	
	
	

}
