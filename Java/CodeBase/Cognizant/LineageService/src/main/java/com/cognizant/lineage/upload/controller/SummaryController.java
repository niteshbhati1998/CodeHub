package com.cognizant.lineage.upload.controller;

import java.util.List;

import org.json.JSONArray;
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
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.SummaryInput;
import com.cognizant.lineage.upload.model.UploadScriptDetails;
import com.cognizant.lineage.upload.model.UploadScriptTypeCount;
import com.cognizant.lineage.upload.service.SummaryService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class SummaryController {

	@Autowired
	private SummaryService summaryService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryController.class);

    @GetMapping("/getScriptDetails/{projectName}")
    public ResponseEntity<?> getScriptDetails(@PathVariable String projectName) {
        CommonResponse<List<UploadScriptDetails>> response = new CommonResponse<>();
        try {
        	List<UploadScriptDetails> uploadScriptUiList = summaryService.getScriptDetails(projectName);
        	response.setMessage("Script details fetched successfully");
        	response.setPayload(uploadScriptUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("Exception occured while fetching Script details "+ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/getScriptTypeCount/{projectName}")
    public ResponseEntity<?> getScriptTypeCount(@PathVariable String projectName) {
        CommonResponse<List<UploadScriptTypeCount>> response = new CommonResponse<>();
        try {
        	List<UploadScriptTypeCount> uploadScriptTypeCountList = summaryService.getScriptTypeCount(projectName);
        	response.setMessage("Script-Type count details fetched successfully");
        	response.setPayload(uploadScriptTypeCountList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("Exception occured while fetching Script-Type count details "+ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/getTechnologyList/{projectName}")
    public ResponseEntity<?> getTechnologyList(@PathVariable String projectName) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
        	JSONArray uiJson = summaryService.getTechnologyList(projectName);
        	response.setMessage("Technology list fetched successfully");
        	response.setPayload(uiJson.toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("Exception occured in getTechnologyList "+ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/fetchDataForTechnology/{projectName}")
    public ResponseEntity<?> fetchDataForTechnology(@PathVariable String projectName, @RequestBody SummaryInput summaryInput) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
        	System.out.println("in service");
        	JSONArray jsonArray = summaryService.fetchTechnologyDetails(projectName, summaryInput.getTechnology());
        	response.setMessage("Data fetched successfully");
            response.setPayload(jsonArray.toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("Exception occured in fetchDataForTechnology "+ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}	