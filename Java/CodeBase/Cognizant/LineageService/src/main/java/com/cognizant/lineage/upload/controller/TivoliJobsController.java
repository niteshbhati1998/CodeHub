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
import com.cognizant.lineage.upload.service.TivoliJobsService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class TivoliJobsController {
	
	@Autowired
	private TivoliJobsService tivoliJobsService;

	private static final Logger LOGGER = LoggerFactory.getLogger(TivoliJobsController.class);
	
	@CrossOrigin
	@PostMapping("/upload/parseTivoliFiles/{projectName}")
	public ResponseEntity<?> parseTivoliScripts(@RequestParam("file") MultipartFile[] files, @PathVariable String projectName) {
		CommonResponse<String> response = new CommonResponse<>();
		String parentTechnology = "Schedular";
		String technology = "Tivoli";
		try {
			Long jobId = tivoliJobsService.uploadTivoliFilesToServer(files, projectName, parentTechnology, technology);
			tivoliJobsService.parseTivoliScripts(jobId, projectName, technology);
			response.setMessage("Tivoli Files Uploaded successfully");
			response.setPayload(String.valueOf(jobId));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in parseTivoliScripts Controller "+ex.getMessage());
			response.setMessage("Exception Occured: "+ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
}
