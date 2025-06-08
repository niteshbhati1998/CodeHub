package com.cognizant.lineage.database.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.database.constants.Constants;
import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.database.model.PythonScriptInput;
import com.cognizant.lineage.database.model.UIInput3;
import com.cognizant.lineage.database.model.UIInput4;
import com.cognizant.lineage.database.model.UIInput5;
import com.cognizant.lineage.database.service.PyvisLibraryLineageService;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.util.Sanitization;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/lineage")
public class PyvisLibraryLineageController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PyvisLibraryLineageController.class);
  
  @Autowired
  PyvisLibraryLineageService pyvisLibraryLineageService;

  //3rd dropdown
  @CrossOrigin
  @GetMapping("/getFilterDropdownList/{projectName}/{type}")
  public ResponseEntity<?> getFilterDropdownList(@PathVariable String projectName, @PathVariable String type,
                                                 @RequestParam("page") Integer pageNo) {
	List<String> filterDropdownList = new ArrayList<>();
      CommonResponse<List<String>> response1 = new CommonResponse<>();
      try {
		filterDropdownList = pyvisLibraryLineageService.getFilterDropdownList(projectName, type, pageNo);
		response1.setMessage("dropdownValues fetched successfully");
		response1.setPayload(filterDropdownList);
    	return new ResponseEntity<>(response1, HttpStatus.OK);
    } catch (Exception e) {
    	response1.setMessage("Exception occurred in getFilterDropdownList");
        LOGGER.info("Exception occurred in getFilterDropdownList Controller" + e.getMessage());
        return new ResponseEntity<>(response1, HttpStatus.BAD_REQUEST);
    } 
  }
  
  //4th dropdown --only for type tech
  @CrossOrigin
  @PostMapping("/getTableNames")
  public ResponseEntity<?> getTableNames(@RequestBody UIInput5 uiInput) {
	List<String> tableNamesList = new ArrayList<>();
      String projectName = Sanitization.sanitizeInput(uiInput.getProjectName());
      String scriptType = Sanitization.sanitizeInput(uiInput.getScriptType());
      CommonResponse<List<String>> response1 = new CommonResponse<>();
	try {
		tableNamesList = pyvisLibraryLineageService.getTableNames(projectName, scriptType);
		response1.setMessage("tableNames fetched successfully");
		response1.setPayload(tableNamesList);
    	return new ResponseEntity<>(response1, HttpStatus.OK);
    } catch (Exception e) {
    	response1.setMessage("Exception occurred while fetching tableNames");
        LOGGER.info("Exception occurred while fetching tableNames Controller" + e.getMessage());
        return new ResponseEntity<>(response1, HttpStatus.BAD_REQUEST);
    } 
  }
  
  //display lineage
  @CrossOrigin
  @PostMapping("/executePyvisLibraryScript")
  public ResponseEntity<?> executePyvisLibraryScript(@RequestBody UIInput3 uiInput) {
	String scriptOutput = "";
      CommonResponse<String> response = new CommonResponse<>();
		uiInput.setProjectName(Sanitization.sanitizeInput(uiInput.getProjectName()));
		uiInput.setTableNames(Sanitization.sanitizeInput(uiInput.getTableNames()));
		uiInput.setDownstreamValue(Sanitization.sanitizeInput(uiInput.getDownstreamValue()));
		uiInput.setFilterNames(Sanitization.sanitizeInput(uiInput.getFilterNames()));
		uiInput.setToggleValue(Sanitization.sanitizeInput(uiInput.getToggleValue()));
		uiInput.setType(Sanitization.sanitizeInput(uiInput.getType()));
		uiInput.setUpstreamValue(Sanitization.sanitizeInput(uiInput.getUpstreamValue()));
    try {
        scriptOutput = pyvisLibraryLineageService.executePyvisLibraryScript(uiInput);
        if(scriptOutput.length()>0) {
            String arr[] = scriptOutput.split("/");
            scriptOutput = scriptOutput.split("/")[arr.length-2] +"/"+scriptOutput.split("/")[arr.length-1].trim();
        }
        response.setMessage("Pyvis script executed successfully");
        response.setPayload(scriptOutput);
        return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setMessage("Exception occurred while executing Pyvis script");
      LOGGER.info("Exception occurred in executePyvisLibraryScript Controller" + e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  //excel generation
  @CrossOrigin
  @PostMapping("/fetchExcelPath")
  public ResponseEntity<?> fetchExcelPath(@RequestBody UIInput4 uiInput) {
    CommonResponse<String> response = new CommonResponse<>();
	String excelPath = "";
    LOGGER.info("fetchExcelPath start");
	uiInput.setProjectName(Sanitization.sanitizeInput(uiInput.getProjectName()));
	uiInput.setTableName(Sanitization.sanitizeInput(uiInput.getTableName()));
    try {
    	excelPath = pyvisLibraryLineageService.executePythonScriptForExcelGeneration(uiInput);
    	response.setMessage(GeneralConstants.SUCCESS);
    	response.setPayload(excelPath);
    	return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setMessage("Error");
      LOGGER.info("Exception occurred in fetchExcelPath Controller" + e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } 
  }

  @CrossOrigin
  @PostMapping("/display-lineage")
  public ResponseEntity<?> executePyvisScript(@RequestBody UIInput3 uiInput) {
      CommonResponse<String> response = new CommonResponse<>();
      try {
          String[] responseArray = pyvisLibraryLineageService.executePythonScriptForDisplayTableLineage(uiInput);
          if (Constants.SUCCESS.equalsIgnoreCase(responseArray[0])) {
              response.setPayload(responseArray[1]);
              response.setMessage("Pyvis script executed successfully");
              return new ResponseEntity<>(response, HttpStatus.OK);
          } else {
              response.setPayload("Json script execution failed");
              response.setMessage(Constants.FAILED);
              return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
          }
      } catch (Exception e) {
          response.setMessage(Constants.FAILED);
          response.setPayload("Exception occurred while executing Pyvis script");
          LOGGER.error("Exception occurred in execute Pyvis Table Lineage Script Controller: ", e);
          return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
  }
  
  //executePythonScript
  @CrossOrigin
  @PostMapping("/executePythonScript")
  public ResponseEntity<?> executePythonScript(@RequestBody PythonScriptInput pythonScriptInput) {
	String scriptOutput = "";
    CommonResponse<String> response = new CommonResponse<>();
    try {
        scriptOutput = pyvisLibraryLineageService.executePythonScript(pythonScriptInput);
        response.setMessage("Python script executed successfully");
        response.setPayload(scriptOutput);
        return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setMessage("Exception occurred while executing Python script");
      LOGGER.info("Exception occurred in executePythonScript Controller" + e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } 
  }

  @CrossOrigin
  @PostMapping("/display-k-graph")
  public ResponseEntity<?> executeKGraphScript(@RequestBody UIInput3 uiInput) {
      CommonResponse<String> response = new CommonResponse<>();
      uiInput.setProjectName(Sanitization.sanitizeInput(uiInput.getProjectName()));
      uiInput.setFilterNames(Sanitization.sanitizeInput(uiInput.getFilterNames()));
      uiInput.setType(Sanitization.sanitizeInput(uiInput.getType()));
      try {
          String[] responseArray = pyvisLibraryLineageService.executeKGraphScript(uiInput);
          if (Constants.SUCCESS.equalsIgnoreCase(responseArray[0])) {
              response.setPayload(responseArray[1]);
              response.setMessage("Knowledge graph script executed successfully");
              return new ResponseEntity<>(response, HttpStatus.OK);
          } else {
              response.setPayload(responseArray[1]);
              response.setMessage(Constants.FAILED);
              return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
          }
      } catch (Exception e) {
          response.setMessage(Constants.FAILED);
          response.setPayload("Exception occurred while executing knowledge graph script");
          LOGGER.error("Exception occurred in execute knowledge graph Script Controller: ", e);
          return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
  }

}