package com.cognizant.lineage.upload.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.InputRequestColumnLineage;
import com.cognizant.lineage.upload.service.ColumnLineageServiceImpl;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("lineage/dbColumnLineage")
public class ColumnLineageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ColumnLineageController.class);

	@Autowired
	ColumnLineageServiceImpl columnLineageService;
	
	@CrossOrigin
	@PostMapping("/triggerColumnLineage")
	public ResponseEntity<?> triggerColumnLineage(@RequestBody InputRequestColumnLineage inputRequest) {
		CommonResponse<String> response = new CommonResponse<>();
		try {

			

			response.setPayload("Column lineage process has been completed.");
			response.setMessage(GeneralConstants.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {

			LOGGER.error("Exception in triggerColumnLineage: ", e);
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
	
	@CrossOrigin
	@PostMapping("/getTables")
	public ResponseEntity<?> getTables(@RequestBody InputRequestColumnLineage inputRequest) {
		CommonResponse<List<String>> response = new CommonResponse<>();
		List<String> names = null;
		try {

			names = columnLineageService.getAllTableNames(inputRequest.getProjectName());

			response.setPayload(names);
			response.setMessage(GeneralConstants.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {

			LOGGER.error("Exception in getTables: ", e);
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@CrossOrigin
	@PostMapping("/getColumnNamesByTableName")
	public ResponseEntity<?> getColumnNamesByTableName(@RequestBody InputRequestColumnLineage inputRequest) {
		CommonResponse<List<String>> response = new CommonResponse<>();
		List<String> names = null;
		try {

			names = columnLineageService.getColumnNamesByTableName(inputRequest.getProjectName(),
					inputRequest.getTableName());

			response.setPayload(names);
			response.setMessage(GeneralConstants.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {

			LOGGER.error("Exception in getColumnNamesByTableName: ", e);
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
	
	@CrossOrigin
	@PostMapping("/displayColumnLineage")
	public ResponseEntity<?> displayColumnLineage(@RequestBody InputRequestColumnLineage inputRequest) {
		CommonResponse<List<String>> response = new CommonResponse<>();
		List<String> names = null;
		try {

			names = columnLineageService.getColumnNamesByTableName(inputRequest.getProjectName(),
					inputRequest.getTableName());

			response.setPayload(null);
			response.setMessage(GeneralConstants.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {

			LOGGER.error("Exception in displayColumnLineage: ", e);
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
}
