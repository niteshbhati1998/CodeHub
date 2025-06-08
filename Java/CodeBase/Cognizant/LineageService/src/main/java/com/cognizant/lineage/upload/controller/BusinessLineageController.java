package com.cognizant.lineage.upload.controller;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.upload.model.ExcelStatus;
import com.cognizant.lineage.upload.service.BusinessLineageService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class BusinessLineageController {
	
	@Autowired
	BusinessLineageService businessLineageService;
	
	@CrossOrigin
	@GetMapping("/getBusinessLineageProjectList")
	public ResponseEntity<?> getBusinessLineageProjectList() {
		Logger LOGGER = businessLineageService.getUtilLoggerObject();
		CommonResponse<List<String>> response = new CommonResponse<>();
		try {
			List<String> projectNamesList = businessLineageService.getProjectNamesList(LOGGER);
			response.setMessage("project names list fetched successfully");
			response.setPayload(projectNamesList);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred in fetching project names list");
			LOGGER.info("Exception occurred in getBusinessLineageProjectList Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}
	
	@CrossOrigin
	@PostMapping("/executeBusinessLineageScript/{projectName}")
	public ResponseEntity<?> executeBusinessLineageScript(@PathVariable String projectName) throws Exception {
		Logger LOGGER = businessLineageService.getUtilLoggerObject();
		CommonResponse<String> response = new CommonResponse<>();
		try {
			String scriptOutput = businessLineageService.executePythonScript(projectName, LOGGER);
			response.setMessage("python script executed successfully");
			response.setPayload(scriptOutput);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.setMessage("Exception occurred while executing python script");
			LOGGER.info("Exception occurred in executeBusinessLineageScript Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}
	
	@CrossOrigin
	@PostMapping("/generateBusinessLineageExcel/{projectName}")
	public ResponseEntity<?> generateBusinessLineageExcel(@PathVariable String projectName) throws Exception {
		Logger LOGGER = businessLineageService.getUtilLoggerObject();
		ExcelStatus response = new ExcelStatus();
		try {
			String excelPath = businessLineageService.generateExcelReport(projectName, LOGGER);
			response.setPayload(excelPath);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getBusinessLineageExcel Controller " + ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}
}