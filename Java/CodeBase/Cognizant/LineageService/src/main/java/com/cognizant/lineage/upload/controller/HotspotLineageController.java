package com.cognizant.lineage.upload.controller;

import java.util.ArrayList;
import java.util.List;

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
import com.cognizant.lineage.upload.model.NodeDataModel;
import com.cognizant.lineage.upload.model.TableRequestModel;
import com.cognizant.lineage.upload.service.HotspotService;
import com.cognizant.lineage.util.Sanitization;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class HotspotLineageController {
	
	@Autowired
	HotspotService hotService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HotspotLineageController.class);
	
	
	@CrossOrigin
	@RequestMapping(value = "/gethotspotdata", method = RequestMethod.POST)
	public ResponseEntity<?> getHotspotdata(@RequestBody TableRequestModel table){
		String projectName = Sanitization.sanitizeInput(table.getProjectName());

		try {
			List<NodeDataModel> nodeData = new ArrayList<>();
			nodeData = hotService.getNodeData(projectName);
			LOGGER.info("Node data for Hotspot fetched successfully.");
			CommonResponse<List<NodeDataModel>> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.SUCCESS);
			response.setPayload(nodeData);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}catch (Exception e) {
			CommonResponse<String> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred.");
			LOGGER.info("Error in getting node data for hotspot: ", e);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@CrossOrigin
	@RequestMapping(value = "/getprojectlist", method = RequestMethod.GET)
	public ResponseEntity<?> getProjectNames(){
		
		try {
			List<String> prjList = new ArrayList<>();
			prjList = hotService.getProjects();
			LOGGER.info("Project name list from node table fetched successfully.");
			CommonResponse<List<String>> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.SUCCESS);
			response.setPayload(prjList);
			return new ResponseEntity<>(response, HttpStatus.OK);
			
		} catch (Exception e) {
			LOGGER.info("Error in getting Project name list from node table.", e);
			CommonResponse<String> response = new CommonResponse<>();
			response.setMessage(GeneralConstants.FAILED);
			response.setPayload("Internal Server error occurred.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
