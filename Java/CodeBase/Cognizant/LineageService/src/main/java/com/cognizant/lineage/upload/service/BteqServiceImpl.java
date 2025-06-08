package com.cognizant.lineage.upload.service;

import java.io.File;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.util.CommonUtil;

@Service
public class BteqServiceImpl implements BteqService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BteqServiceImpl.class);
	
	@Value("${bteqInputFiles}")
	private String bteqUploadFileLocation;
	
	@Value("${bteqOutputFileLocation}")
	private String outputFileLocation;
	
	@Value("${logFileLocation}")
	private String logFileDirectory;

	@Autowired
	LineageJobRepository jobRepo;
	
	@Autowired 
	ExecuteScriptForTptAndPowerbi tptAndPowerbi;
	
	@Autowired
	ExecuteScriptForKsh kshScript;
	
	@Autowired
	ExecuteScriptForQlikView qvScriptService;
	
	@Autowired
	ExecuteScript executeScript;
	
	@Override
	public LineageJob uploadFilesAndSaveLineage(MultipartFile[] files, String tech, String projectName) throws LineageBusinessException {	
		try {
			Long jobId = jobRepo.getJobIdByMax();

			LineageJob lineageJob = new LineageJob();
			lineageJob.setJobId(jobId);
			lineageJob.setProjectName(projectName);
			lineageJob.setTechnology(tech);
			lineageJob.setStartTime(new Date());
			lineageJob.setJobParams(TechnologyConstants.TECH_PARAMS.get(tech));
			lineageJob.setUploadType(GeneralConstants.UPLOAD_TYPE);
		
			lineageJob = jobRepo.save(lineageJob);
			jobId = lineageJob.getJobId();
			
			LOGGER.info("No of files in Bteq = {}", files.length);
			LOGGER.info("Save location for Bteq = " + bteqUploadFileLocation + "/" + jobId);			
			
			File newInputDirectory = new File(bteqUploadFileLocation + File.separator + jobId);
			newInputDirectory.mkdir();
			
			File newOutputDirectory = new File(outputFileLocation + File.separator + jobId);
			newOutputDirectory.mkdir();
			
			/**
			for (MultipartFile file : files) {
				
				if(file.getOriginalFilename().toLowerCase().endsWith(GeneralConstants.ZIP_FILE_FORMAT)) {
					CommonUtil.unzipMultipartFileForBteq(file, newInputDirectory.getAbsolutePath());
					continue;
				}
				
				byte[] bytes = file.getBytes();
				Path path = Paths.get(newInputDirectory.getAbsolutePath() + File.separator + file.getOriginalFilename());
				//canonical path to defer vulnerability
				Path canonicalPath = path.normalize();
				
				Files.write(canonicalPath, bytes);
			}**/
			
			CommonUtil.uploadAllScriptsToInputLocation(files,newInputDirectory.getAbsolutePath());
			LOGGER.info("Bteq Uploaded files successfully and invoked shell script for processing");			
			
			if (TechnologyConstants.TPT.equalsIgnoreCase(tech) ||
					TechnologyConstants.POWERBI.equalsIgnoreCase(tech)) {
					//execute python then step 1-12 as next steps
					tptAndPowerbi.executeZeroScript(newInputDirectory.getAbsolutePath(),
							newOutputDirectory.getAbsolutePath(), logFileDirectory, Long.toString(jobId), 
							tech.toLowerCase(), projectName);
					
			} else if(TechnologyConstants.PYTHON_SCRIPT.equalsIgnoreCase(tech)) { 
	    	    tptAndPowerbi.executePythonScript(newInputDirectory.getAbsolutePath(),
					    newOutputDirectory.getAbsolutePath(), logFileDirectory, Long.toString(jobId), 
					    tech.toLowerCase(), projectName);
	        } else if (TechnologyConstants.SHELL_1.equalsIgnoreCase(tech)) {
					//handle ksh
					kshScript.executeFirstScript(newInputDirectory.getAbsolutePath(),
							newOutputDirectory.getAbsolutePath(),
							logFileDirectory, Long.toString(jobId), tech.toLowerCase(), projectName);
					
			} else if (TechnologyConstants.QLIKVIEW.equalsIgnoreCase(tech)) {
					qvScriptService.executeFirstScript(newInputDirectory.getAbsolutePath(),
							newOutputDirectory.getAbsolutePath(),logFileDirectory,
							Long.toString(jobId), tech.toLowerCase(), projectName);
					
			} else if(TechnologyConstants.MLOAD.equalsIgnoreCase(tech)) {
				    executeScript.executeMLoadScript(newInputDirectory.getAbsolutePath(), Long.toString(jobId),
							tech.toLowerCase(), projectName, logFileDirectory);
		    } else {
					//handle bteq, procedure, view etc.
					executeScript.executeFirstScript(newInputDirectory.getAbsolutePath(),
							newOutputDirectory.getAbsolutePath(), logFileDirectory, Long.toString(jobId), 
							tech.toLowerCase(), projectName);
			}
			return lineageJob;
			
		} catch (LineageBusinessException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new LineageRuntimeException(e.getMessage());
		}
	}
}
