package com.cognizant.assessment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.assessment.service.LocationService;

@RestController
@RequestMapping("/location")
public class LocationController {

	@Autowired
	private LocationService locationService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationController.class);
			
	@GetMapping("/update")
	public String updateLocation() {
		String message = "";
		try {
			String location = "(" + Math.round(Math.random()*100) +","+ Math.round(Math.random()*100) + ")";
			locationService.updateLocation(location);
			message = "location updated successfully";
		} catch(Exception ex) {
			LOGGER.error("Exception occured while updating location "+ex.getMessage());
		}
		return message;
	}
}
