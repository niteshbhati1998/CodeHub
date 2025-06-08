package com.cognizant.assessment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cognizant.assessment.util.AppConstants;

@Service
public class LocationService {
	
	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

	public void updateLocation(String location) {
		this.kafkaTemplate.send(AppConstants.TOPIC_NAME, location);
	}
}
   