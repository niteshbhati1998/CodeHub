package com.example.jpa.controller;

import com.example.jpa.entity.Applicant;
import com.example.jpa.model.Response;
import com.example.jpa.service.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applicant")
public class ApplicantController {

    @Autowired
    private ApplicantService applicantService;

    @PostMapping("/save")
    public ResponseEntity<Response> saveApplicant(@RequestBody Applicant applicant) {
        Response response = new Response();
        try {
            Applicant savedApplicant = applicantService.saveApplicant(applicant);
            response.setMessage("success");
            response.setPayload(savedApplicant);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<Response> getAllApplicants() {
        Response response = new Response();
        try {
            List<Applicant> applicantsList = applicantService.getAllApplicants();
            response.setMessage("success");
            response.setPayload(applicantsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
