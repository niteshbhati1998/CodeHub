package com.cognizant.lineage.upload.controller;

import java.io.IOException;
import java.util.List;
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

import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.upload.model.ModuleInfo;
import com.cognizant.lineage.upload.model.ModuleLineage;
import com.cognizant.lineage.upload.model.ModuleLineageUi;
import com.cognizant.lineage.upload.model.UiInputModuleInfo;
import com.cognizant.lineage.upload.service.ModuleLineageService;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class ModuleLineageController {
	
	@Autowired
    LoggerUtil loggerUtil;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Autowired
	ModuleLineageService moduleLineageService;
	
	@CrossOrigin
	@GetMapping("/getSchemaAndModuleList/{projectName}")
	public ResponseEntity<?> getSchemaAndModuleList(@PathVariable String projectName) throws Exception {
		CommonResponse<ModuleLineageUi> response = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ModuleLineage.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		LOGGER.info("start..................................................................");
		try {
			ModuleLineageUi moduleLineageUi = moduleLineageService.getSchemaAndModuleList(projectName, LOGGER);
			response.setMessage("Schema list and Module list fetched successfully");
			response.setPayload(moduleLineageUi);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred in fetching Schema and Module list");
			LOGGER.info("Exception occurred in getSchemaAndModuleList Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		finally {
			LOGGER.info("end....................................................................");
		}
	}
	
	@CrossOrigin
	@PostMapping("/addModuleInfo/{projectName}")
	public ResponseEntity<?> addModuleInfo(@PathVariable String projectName, @RequestBody ModuleInfo moduleInfo) throws Exception {
		CommonResponse<List<String>> response = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ModuleLineage.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		LOGGER.info("start..................................................................");
		try {
			moduleLineageService.addModuleInfo(projectName, moduleInfo.getTitle(), LOGGER);
			response.setMessage("moduleInfo added successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred while adding moduleInfo");
			LOGGER.info("Exception occurred in addModuleInfo Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		finally {
			LOGGER.info("end....................................................................");
		}
	}
	
	@CrossOrigin
	@PostMapping("/executePythonScriptForModuleLineage/{projectName}")
	public ResponseEntity<?> executePythonScript(@PathVariable String projectName, @RequestBody final ModuleLineage moduleLineage) throws Exception {
		CommonResponse<List<String>> response = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ModuleLineage.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		LOGGER.info("start..................................................................");
		projectName = Sanitization.sanitizeInput(projectName);
		try {
			moduleLineageService.executePythonScript(projectName, moduleLineage.getMap(), LOGGER);
			response.setMessage("PythonScript executed successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred while executing PythonScript");
			LOGGER.info("Exception occurred in executePythonScript Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		finally {
			LOGGER.info("end....................................................................");
		}
	}
	
	@CrossOrigin
	@PostMapping("/deleteModule")
	public ResponseEntity<?> deleteModuleInfo(@RequestBody UiInputModuleInfo moduleInfo) throws Exception {
		CommonResponse<List<String>> response = new CommonResponse<>();
		Logger LOGGER;
		FileHandler handler = null;
		String logFileName = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "ModuleLineage.log");
			try {
				handler = new FileHandler(logFileName, true);
			} catch (SecurityException | IOException e) {
				throw new Exception(e);
			}
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		LOGGER.info("start..................................................................");
		try {
			moduleLineageService.deleteModuleInfo(moduleInfo.getProjectName(), moduleInfo.getModuleName(), LOGGER);
			response.setMessage("moduleInfo deleted successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred while deleting moduleInfo");
			LOGGER.info("Exception occurred in deleteModuleInfo Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		finally {
			LOGGER.info("end....................................................................");
		}
	}
	
	@CrossOrigin
	@PostMapping("/getModuleLineageStatus/{projectName}")
	public ResponseEntity<?> getModuleLineageStatus(@PathVariable String projectName) throws Exception {
		CommonResponse<String> response = new CommonResponse<>();
		try {
			moduleLineageService.getModuleLineageStatus(projectName, response);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred in getModuleLineageStatus");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}
}