package com.example.jpa.controller;

import com.example.jpa.entity.Applicant;
import com.example.jpa.model.Response;
import com.example.jpa.service.ApplicantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Response> saveApplicant(@Valid @RequestBody Applicant applicant) {
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
    public ResponseEntity<Response> getApplicants() {
        Response response = new Response();
        try {
            List<Applicant> applicantsList = applicantService.getApplicants();
            response.setMessage("success");
            response.setPayload(applicantsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getByLastName")
    public ResponseEntity<Response> getByLastName(@RequestParam String lastName) {
        Response response = new Response();
        try {
            List<Applicant> applicantsList = applicantService.getByLastName(lastName);
            response.setMessage("success");
            response.setPayload(applicantsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getByValue")
    public ResponseEntity<Response> getByValue(@RequestParam String value) {
        Response response = new Response();
        try {
            List<Applicant> applicants = applicantService.getByValue(value);
            response.setMessage("success");
            response.setPayload(applicants);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<Response> updateApplicant(@Valid @RequestBody Applicant applicant) {
        Response response = new Response();
        try {
            Applicant updatedApplicant = applicantService.updateApplicant(applicant);
            response.setMessage("success");
            response.setPayload(updatedApplicant);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getByPage")
    public ResponseEntity<Response> getByPage(@RequestParam int page, @RequestParam int size) {
        Response response = new Response();
        try {
            Page<Applicant> applicantsList = applicantService.getByPage(page, size);
            response.setMessage("success");
            response.setPayload(applicantsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
