package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.entity.TivoliJob;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.dao.repository.TivoliJobRepository;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class TivoliJobsService {
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${TivoliFileUploadLocation}")
	private String tivoliFileUploadLocation;

	@Autowired
	public LineageJobRepository lineageJobRepository;
	
	@Autowired
	public LineageJobStatusRepository lineageJobStatusRepository;
	
	@Autowired
	public TivoliJobRepository tivoliJobRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(TivoliJobsService.class);
	
	public Long uploadTivoliFilesToServer(MultipartFile[] files, String projectName, String parentTechnology, String technology) {
		Long jobId = lineageJobRepository.getJobIdByMax();
		try {
			projectName = Sanitization.sanitizeInput(projectName);	
			parentTechnology = Sanitization.sanitizeInput(parentTechnology);
			technology = Sanitization.sanitizeInput(technology);	
			
			String fileUploadLocation = tivoliFileUploadLocation +"/"+ jobId;
			
			//saving details in lineage_job
			LineageJob lineageJob = LineageJob.builder()
					.jobId(jobId)
					.projectName(projectName)
					.jobParams(parentTechnology)
					.technology(technology)
					.uploadType(GeneralConstants.UPLOAD_TYPE)
					.uploadDir(fileUploadLocation)
					.startTime(new Date())
					.build();
			lineageJobRepository.saveAndFlush(lineageJob);

			LOGGER.info("Uploading Tivoli Files to server");
			CommonUtil.uploadAllScriptsToInputLocation(files, fileUploadLocation);
			LOGGER.info("Tivoli Files uploaded successfully");

			//saving details in lineage_job_status
			File file = new File(fileUploadLocation);
			File[] filesArr = file.listFiles();
			
			LineageJobStatus lineageJobStatus = LineageJobStatus.builder()
					.jobId(jobId)
					.stepNo(1)
					.stepName("Tivoli File Parsing")
					.noOfFileReceived(filesArr.length)
					.noOfFileProcessed(0)
					.status("Processing")
					.logFileLocation(logFileLocation + "LineageService.log")
					.build();
			lineageJobStatusRepository.saveAndFlush(lineageJobStatus);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in uploadTivoliFilesToServer Service "+ex.getMessage());
		}
		return jobId;
	}
	
	@Async
	public void parseTivoliScripts(Long jobId, String projectName, String technology) {
		BufferedReader reader = null;
		try {
			File file = new File(tivoliFileUploadLocation +"/"+ jobId);
			File[] fileArr = file.listFiles();
			StringBuilder jobStatusDetails = null;
			for(int i=0;i<fileArr.length;i++) {
				boolean startParsing = false;
				int counter = 0;

				LOGGER.info("File: "+fileArr[i].getName());
				reader = new BufferedReader(new FileReader(fileArr[i]));
				String line;

				String task = "";
				List<String> followUpTask = new ArrayList<>();
				String jobName = "";
				String workflowName = "";
				String description = "";
				TivoliJob tivoliJob = null;
				while ((line = reader.readLine()) != null) {
					System.out.println("line..............."+line);
					line = line.toUpperCase().trim();

					if(line.startsWith("MOSPOOL01") && counter==0) {
						startParsing = true;
					}
					if(startParsing) {
						if(line.startsWith("MOSPOOL01")) {
							if(counter==0) {
								task = line.split("#")[1];
								counter++;
							} else {
								for(String followTask: followUpTask) {
									tivoliJob = TivoliJob.builder()
											.jobId(jobId)
											.projectName(projectName)
											.fileName(fileArr[i].getName())
											.task(task)
											.followUpTask(followTask)
											.jobName(jobName)
											.workflowName(workflowName)
											.description(description)
											.build();
									if(!followTask.trim().equals("")) {
										tivoliJobRepository.saveAndFlush(tivoliJob);
									}
								};
								task = "";
								followUpTask = new ArrayList<>();
								jobName = "";
								workflowName = "";
								description = "";

								task = line.split("#")[1];
							}
						} else if(line.startsWith("FOLLOWS")) {
							followUpTask.add(line.split(" ")[1]);
						} else if(line.startsWith("SCRIPTNAME")) {
							String[] wordsArr = line.split(" ");
							jobName = wordsArr[wordsArr.length-1].replace("\"", "");
							
					    	for(int j=wordsArr.length-1;j>=0;j--) {
					    		if(wordsArr[j].contains(".")) {
					    			workflowName = wordsArr[j].split("/")[1];
					    			break;
					    		}
					    	}
						}  else if(line.startsWith("DESCRIPTION")) {
							description = line.split(" ", 2)[1].replace("\"", "");
						}
					}
				}
				for(String followTask: followUpTask) {
					tivoliJob = TivoliJob.builder()
							.jobId(jobId)
							.projectName(projectName)
							.fileName(fileArr[i].getName())
							.task(task)
							.followUpTask(followTask)
							.jobName(jobName)
							.workflowName(workflowName)
							.description(description)
							.build();
					if(!followTask.trim().equals("")) {
						tivoliJobRepository.saveAndFlush(tivoliJob);
					}
				};
				
				jobStatusDetails = new StringBuilder();
				if(i==fileArr.length-1) {
					jobStatusDetails.append("No of Files Received: "+fileArr.length+",");
					jobStatusDetails.append("No of Files Processed: "+(i+1)+",");
					jobStatusDetails.append("LogFileLocation: "+logFileLocation+"LineageService.log");
					lineageJobStatusRepository.updateNoOfFilesProcessedAndJobStatusDetails(i+1,jobStatusDetails.toString(),"Completed",jobId,1);
				} else {
					jobStatusDetails.append("No of Files Received: "+fileArr.length+",");
					jobStatusDetails.append("No of Files Processed: "+(i+1));
					lineageJobStatusRepository.updateNoOfFilesProcessedAndJobStatusDetails(i+1,jobStatusDetails.toString(),"Processing",jobId,1);
				}
			}
			
			executePythonScript(jobId, projectName, technology);
		} catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occured in parseTivoliScripts Service "+ex.getMessage());
		}
	}
	
	public void executePythonScript(Long jobId, String projectName, String technology) {
		try {		
			projectName = Sanitization.sanitizeInput(projectName);
			technology = Sanitization.sanitizeInput(technology).toLowerCase();
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName, csrfToken, String.valueOf(jobId),  projectName, technology.toLowerCase());
			LOGGER.info("python3.9" +" "+ commonScriptLocation +"/"+ commonScriptName +" "+ csrfToken +" "+ jobId +" "+ projectName +" "+ technology.toLowerCase());
			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in executePythonScript Service "+ex.getMessage());
		}
	}
}

