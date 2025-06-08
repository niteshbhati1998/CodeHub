package com.cognizant.lineage.pyspark.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.pyspark.model.Response;
import com.cognizant.lineage.pyspark.services.impl.ProcessLine;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class PySparkLineageController {

    private static Logger LOGGER = LoggerFactory.getLogger(PySparkLineageController.class);

    @Autowired
    private ProcessLine processLine;

    @CrossOrigin
    @RequestMapping(value = "/lineage/{tech}/{projectName}", method = RequestMethod.POST)
    public ResponseEntity<?> generatePysparkLineage(@RequestParam("file") MultipartFile[] files, @PathVariable String tech, @PathVariable String projectName) {
    	Response response = new Response();
    	try {
    		LineageJob lineageJob = processLine.uploadFilesToServer(files, tech, projectName);
			processLine.processEachLine("util", lineageJob);
			processLine.processEachLine("pysaprk", lineageJob);
			
			response.setMessage("JobId fetched successfully");
			response.setPayload(String.valueOf(lineageJob.getJobId()));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.error("Exception occured in  generatePysparkLineage Controller ", ex.getMessage());
			response.setMessage("Exception occured in  generatePysparkLineage Controller");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
}