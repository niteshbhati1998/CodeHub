package com.cognizant.lineage.upload.controller;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.upload.dao.DataStageDAO;
import com.cognizant.lineage.upload.service.DataStageService;
import com.cognizant.lineage.util.LoggerUtil;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class DataStageController {
	
	@Autowired
    LoggerUtil loggerUtil;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Autowired
	DataStageDAO dataStageDAO;
	
	@Autowired
	DataStageService dataStageService;
	
    //private static final Logger LOGGER = LoggerFactory.getLogger(DataStageController.class);

	@CrossOrigin
	@PostMapping("/datastageUpload/{projectName}")
	public ResponseEntity<?> uploadDataStageXmlsToServer(@RequestParam("file") MultipartFile[] files, @PathVariable String projectName) throws Exception {
		CommonResponse<Integer> response = new CommonResponse<>();
		int jobId = dataStageDAO.getJobId();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "DataStage_"+jobId+".log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		LOGGER.info("start..................................................................");
		try {
			dataStageService.insertIntoLineageJob(jobId, projectName, LOGGER);
			dataStageService.uploadFilesToServer(files, jobId, LOGGER);
			dataStageService.parseDataStageXmls(jobId, projectName, logFileName, LOGGER);
			response.setMessage("DataStage Xml Files Uploaded Successfully");
			response.setPayload(jobId);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred in uploadDataStageXmlsToServer Controller");
			LOGGER.info("Exception occurred in uploadDataStageXmlsToServer Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}
}