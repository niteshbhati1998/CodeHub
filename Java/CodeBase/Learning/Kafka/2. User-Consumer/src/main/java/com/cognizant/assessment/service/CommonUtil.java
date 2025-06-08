package com.cognizant.assessment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.cognizant.assessment.util.AppConstants;

@Service
public class CommonUtil {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

	@KafkaListener(topics = AppConstants.TOPIC_NAME, groupId = AppConstants.GROUP_ID)
	public void updatedLocation(String location) {
		LOGGER.info("location: "+location);
	}
}
