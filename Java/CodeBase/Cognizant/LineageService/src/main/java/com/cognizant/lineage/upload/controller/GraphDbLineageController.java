package com.cognizant.lineage.upload.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.DatabaseDetails;
import com.cognizant.lineage.upload.model.Lineage;
import com.cognizant.lineage.upload.service.GraphDbLineageService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class GraphDbLineageController {
	
	@Autowired
	private GraphDbLineageService graphDbLineageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphDbLineageController.class);

	@CrossOrigin
	@RequestMapping(value = "/database", method = RequestMethod.POST)
	public ResponseEntity<?> getDatabase(@RequestBody Lineage database) {
		try {
			Set<DatabaseDetails> databaseWithTableMapping = new HashSet<>();
			databaseWithTableMapping = graphDbLineageService.getDatabase(database.getProjectName());
			CommonResponse<Set<DatabaseDetails>> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.SUCCESS);
			response.setPayload(databaseWithTableMapping);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception in getting getDatabase()in service, ", e);
			CommonResponse<String> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.EXCEPTION_IN_GETTING_SUMMARY_TABLE);
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@CrossOrigin
	@RequestMapping(value = "/table", method = RequestMethod.POST)
	public ResponseEntity<?> getTable(@RequestBody Lineage database) {
		try {
			List<String> respList = new ArrayList<>();
			respList = graphDbLineageService.dbTableJoinedOnDot(database);
			CommonResponse<List<String>> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.SUCCESS);
			response.setPayload(respList);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			CommonResponse<String> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.EXCEPTION_IN_GETTING_TABLE_DETAILS);
			LOGGER.error("Exception in getting getTable() in service ", e);
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}
	}
}
