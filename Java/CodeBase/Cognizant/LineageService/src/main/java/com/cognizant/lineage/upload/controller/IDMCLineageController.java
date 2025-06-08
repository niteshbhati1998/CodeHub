package com.cognizant.lineage.upload.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.IDMCLineageService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class IDMCLineageController {
	
	@Autowired
	private IDMCLineageService idmcLineageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(IDMCLineageController.class);
	
	@CrossOrigin
	@PostMapping("/upload/parseIdmcFiles/{projectName}")
	public ResponseEntity<?> parseIdmcJsonFiles(@RequestParam("file") MultipartFile[] files, @PathVariable String projectName) {
		CommonResponse<String> response = new CommonResponse<>();
		String technology = "IDMC";
		try {
			Long jobId = idmcLineageService.uploadFilesToServer(files, projectName, technology);
			idmcLineageService.startIDMCProcess(jobId, projectName, technology);
			response.setMessage("Files Uploaded successfully");
			response.setPayload(String.valueOf(jobId));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in parseIdmcJsonFiles Controller "+ex.getMessage());
			response.setMessage("Exception Occured: "+ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
}
