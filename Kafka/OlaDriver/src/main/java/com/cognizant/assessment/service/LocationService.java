package com.cognizant.assessment.service;

import com.cognizant.assessment.constant.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void updateLocation(String currentLocation) {
        kafkaTemplate.send(AppConstants.TOPIC_DRIVER_LOCATION, AppConstants.TOPIC_DRIVER_LOCATION_KEY, currentLocation);
    }
}
   