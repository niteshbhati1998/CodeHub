package com.cognizant.lineage.database.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.database.dao.ImportDBObjectsDAO;
import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.database.model.DbScriptDetails;
import com.cognizant.lineage.database.model.Response4;
import com.cognizant.lineage.database.model.UIInput1;
import com.cognizant.lineage.database.service.ImportDBObjectsService;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.util.LoggerUtil;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class ImportDBObjectsController {
 
    @Autowired
    LoggerUtil loggerUtil;
	
    @Value("${logFileLocation}")
    private String logFileLocation;

    @Autowired
    ImportDBObjectsDAO importDBObjectsDAO;
  
    @Autowired
    ImportDBObjectsService importDBObjectsService;
  
	@GetMapping("/getDatabaseTypeList")
	public ResponseEntity<?> getDatabaseTypeList() throws Exception {
		CommonResponse<List<String>> res = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "DatabaseTypeList.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		try {
			Set<String> databaseTypeList = importDBObjectsService.getDatabaseTypeList(LOGGER);
			res.setMessage("databaseTypeList fetched successfully");
			res.setPayload(new ArrayList<>(databaseTypeList));
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception ex) {
			res.setMessage("Exception occurred while fetching databaseTypeList");
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
	}
  
	@PostMapping("/getConnectionNameList/{databaseType}")
	public ResponseEntity<?> getDatabaseListForType(@PathVariable String databaseType) throws Exception {
		CommonResponse<List<String>> res = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		databaseType = databaseType.contains("_") ? databaseType.replaceAll("_", " ") : databaseType;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ConnectionNameList.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		try {
			List<String> connectionNameList = importDBObjectsService.getConnectionNameList(databaseType, LOGGER);
			res.setMessage("ConnectionNameList fetched successfully");
			res.setPayload(connectionNameList);
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			res.setMessage("Exception occurred while fetching ConnectionNameList");
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
	}
  
	@CrossOrigin
	@PostMapping("/extract")
	public ResponseEntity<?> extract(@RequestBody UIInput1 uiInputList) throws Exception {
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		Response4 response = new Response4();
		HashMap<Integer, String> hm = new HashMap<>();
		synchronized (this) {
			logFileName = new String(logFileLocation + "Extract.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		try {			
			LOGGER.info("Start......................................................................................................................");	
			importDBObjectsService.sanitizeInputs(uiInputList, LOGGER);
			
			LOGGER.info("Step1: saving user-provided ui details in semantic.lineage_job");
			List<Integer> jobIdList = importDBObjectsService.saveDatabaseObjectTypeDetails(uiInputList, LOGGER);
			LOGGER.info("Step1: user-provided details saved successfully");
			
			//fetching saved details
			List<DbScriptDetails> dbScriptDetailsList = importDBObjectsDAO.getDbScriptDetails(jobIdList, LOGGER);
			for(DbScriptDetails dbScriptDetails: dbScriptDetailsList) {
				hm.put(dbScriptDetails.getJobId(), dbScriptDetails.getDatabaseObjectType());
			}		
			importDBObjectsService.executeExtractFunctionality(dbScriptDetailsList, uiInputList, response, LOGGER);
			response.setMessage("JobId and ObjectType mapping fetched successfully");
			response.setPayload(hm);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			CommonResponse<String> response1 = new CommonResponse<>();
			response1.setMessage(GeneralConstants.FAILED);
			response1.setPayload("Internal Server error occurred");
			return new ResponseEntity<>(response1, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			LOGGER.info("End........................................................................................................................");
		}
	}

	@CrossOrigin
	@GetMapping("/extractStatus/{jobId}")
	public ResponseEntity<?> extractStatus(@PathVariable String jobId) throws Exception {
		CommonResponse<String> response = new CommonResponse<>();
		try {
			System.out.println("Start.................................................................");
			String status = importDBObjectsService.getDbObjectStatus(Integer.parseInt(jobId));
			System.out.println("sending status : " + status + " to UI");
			response.setPayload(status);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			System.out.println("Exception occurred in status button Controller" + ex.getMessage());
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			System.out.println("End....................................................................");
		}
	}
}
