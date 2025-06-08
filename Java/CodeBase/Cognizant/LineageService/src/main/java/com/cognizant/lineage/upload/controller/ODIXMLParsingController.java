package com.cognizant.lineage.upload.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;
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

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.database.model.DbScriptDetails;
import com.cognizant.lineage.database.model.Response4;
import com.cognizant.lineage.database.model.UIInput1;
import com.cognizant.lineage.database.service.ImportDBObjectsService;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.ODIXMLParsingService;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class ODIXMLParsingController {
	
	@Autowired
	ODIXMLParsingService odiXMLParsingService;

	@Autowired
	ImportDBObjectsService importDBObjectsService;
	
	@Autowired
    LoggerUtil loggerUtil;
	
    @Value("${logFileLocation}")
    private String logFileLocation;
	
	public static int runId;
	
   private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ODIXMLParsingController.class);

	@CrossOrigin
	@PostMapping("/parseODIXml/Oracle/{tech}/{projectName}")
	public ResponseEntity<?> parseODIXml(@RequestParam("file") MultipartFile[] files, 
			@PathVariable String tech, @PathVariable String projectName) throws Exception {
		CommonResponse<String> response = new CommonResponse<>();
		Logger LOG;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "OracleUpload.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOG = loggerUtil.getLogger(handler, logFileName);
		}
		try {
			tech = Sanitization.sanitizeInput(tech);
			projectName = Sanitization.sanitizeInput(projectName);
			Map<String, Object> requiredObjects = odiXMLParsingService.parseODIXmlAndSaveLineage(files, tech, projectName);
			
			LineageJob lineageJob = (LineageJob) requiredObjects.get(GeneralConstants.LINEAGE_JOB);
			Long nextSequenceId = lineageJob.getJobId();
			LOGGER.info("job id is..........{}", nextSequenceId);
			
			tech = Sanitization.sanitizeInput(String.valueOf(requiredObjects.get(GeneralConstants.TECH)));
			projectName = Sanitization.sanitizeInput(String.valueOf(requiredObjects.get(GeneralConstants.PROJECT_NAME)));
			File filesArr[] = (File[]) requiredObjects.get(GeneralConstants.FILES_ARRAY);
			LineageJobStatus jobStatus = (LineageJobStatus) requiredObjects.get(GeneralConstants.JOB_STATUS);

			if(TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
				odiXMLParsingService.parseODIXML(tech, filesArr, nextSequenceId.intValue(),
						projectName, jobStatus, lineageJob);
			} else {
				Response4 res = new Response4();
				List<DbScriptDetails> dbScriptDetailsList = new ArrayList<>();
				DbScriptDetails dbScriptDetails = new DbScriptDetails();
				dbScriptDetails.setJobId(lineageJob.getJobId().intValue());
				dbScriptDetails.setProjectName(Sanitization.sanitizeInput(lineageJob.getProjectName()));
				dbScriptDetails.setDatabaseType(Sanitization.sanitizeInput(lineageJob.getJobParams()));
				dbScriptDetails.setDatabaseObjectType(tech);
				dbScriptDetails.setLocation(Sanitization.sanitizeInput(lineageJob.getUploadDir()));
				dbScriptDetailsList.add(dbScriptDetails);
				dbScriptDetails.setUploadType(Sanitization.sanitizeInput(lineageJob.getUploadType()));
				UIInput1 uiInputList = new UIInput1();
				uiInputList.setDatabaseType(Sanitization.sanitizeInput(lineageJob.getJobParams()));
				uiInputList.setProjectName(Sanitization.sanitizeInput(lineageJob.getProjectName()));
				importDBObjectsService.executeExtractFunctionality(dbScriptDetailsList, uiInputList, res, LOG);
			}			
			response.setPayload(String.valueOf(nextSequenceId));
			response.setMessage(GeneralConstants.PARSING_DONE);
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (LineageBusinessException e) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred into parseODIXml Controller " + ex.getMessage());
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.EXCEPTION_WHILE_ODI_XML_PARSING);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}