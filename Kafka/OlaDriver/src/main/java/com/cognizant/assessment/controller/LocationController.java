package com.cognizant.assessment.controller;

import com.cognizant.assessment.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/update")
    public ResponseEntity<?> updateLocation() throws InterruptedException {
        int range = 500;
        while(range>0) {
            locationService.updateLocation(String.valueOf(range));
            Thread.sleep(1000);
            range--;
        }
        return new ResponseEntity<>(Map.of("message", "location updated successfully"), HttpStatus.OK);
    }
}
