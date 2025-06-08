package com.cognizant.lineage.upload.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.DashboardRequest;
import com.cognizant.lineage.upload.service.ProjectButtonService;
import com.cognizant.lineage.util.Sanitization;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class ProjectButtonController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectButtonController.class);
	
	@Autowired
	private ProjectButtonService projectButtonService;
	
	@CrossOrigin
	@RequestMapping(value = "/insertprojectname/{projectName}", method = RequestMethod.POST)
	public ResponseEntity<?> projectNameInsertion(@PathVariable String projectName) {
		CommonResponse<String> response = new CommonResponse<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			projectButtonService.projectNameInsertion(projectName);
			response.setMessage(GeneralConstants.SUCCESS);
			response.setPayload(GeneralConstants.PROJECT_NAME_INSERTED);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception in projectNameInsertion controller: ", e);
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.EXCEPTION_INSERTING_PROJECT_NAME);
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@CrossOrigin
	@RequestMapping(value = "/gettingprojectnames", method = RequestMethod.GET)
	public ResponseEntity<?> gettingProjectNames() {
	
		try {
			List<String> projectList = projectButtonService.gettingProjectNames();
			CommonResponse<List<String>> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.SUCCESS);
			response.setPayload(projectList);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception in gettingProjectNames controller: ", e);
			CommonResponse<String> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.EXCEPTION_GETTING_PROJECT_NAMES);
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping("/dashboard-warning")
	public ResponseEntity<?> getDashboardWarning(@RequestBody DashboardRequest dashboardRequest) {
		CommonResponse<String> response = new CommonResponse<>();
		if (StringUtils.isEmpty(dashboardRequest.getProjectName())) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.INVALID_INPUT);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		if (StringUtils.isEmpty(dashboardRequest.getDashboardName())) {
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Please input the dashboard name");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		try {
			String responseString = projectButtonService.getDashboardWarning(dashboardRequest);
			if (GeneralConstants.SUCCESS.equalsIgnoreCase(responseString)) {
				response.setMessage(GeneralConstants.SUCCESS);
			} else {
				response.setMessage(GeneralConstants.FAILED);
			}
			response.setPayload(responseString);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.info("Exception in controller getDashboardWarning {}", e.getMessage());
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload(GeneralConstants.INTERNAL_ERROR_OCCURRED);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
