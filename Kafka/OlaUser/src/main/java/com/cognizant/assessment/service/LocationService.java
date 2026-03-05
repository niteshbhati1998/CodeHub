package com.cognizant.assessment.service;

import constant.AppConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    @KafkaListener(topics = AppConstants.TOPIC_DRIVER_LOCATION, groupId = AppConstants.GROUP_ID)
    public void updatedLocation(String location) {
        System.out.println("current location: " + location);
    }
}
