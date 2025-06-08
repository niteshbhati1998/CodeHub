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
import com.cognizant.lineage.upload.service.SSISService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class SSISController {
	
	@Autowired
	private SSISService ssisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(SSISController.class);
	
	@CrossOrigin
	@PostMapping("/upload/parseSSISScripts/{projectName}")
	public ResponseEntity<?> parseSSISScripts(@RequestParam("file") MultipartFile[] files, @PathVariable String projectName) {
		CommonResponse<String> response = new CommonResponse<>();
		String parentTechnology = "ETL";
		String technology = "SSIS";
		try {
			Long jobId = ssisService.uploadSSISScriptsToServer(files, projectName, parentTechnology, technology);
			ssisService.parseSSISScripts(jobId, projectName, technology);
			response.setMessage("SSIS scripts Uploaded successfully");
			response.setPayload(String.valueOf(jobId));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.info("Exception occured while parsing SSIS scripts "+ex.getMessage());
			response.setMessage("Exception occured: "+ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
}
