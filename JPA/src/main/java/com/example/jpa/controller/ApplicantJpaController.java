package com.example.jpa.controller;

import com.example.jpa.entity.Applicant;
import com.example.jpa.model.Response;
import com.example.jpa.service.ApplicantJpaService;
import com.example.jpa.service.ApplicantPagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applicant")
public class ApplicantJpaController {

    @Autowired
    private ApplicantJpaService applicantJpaService;

    @GetMapping("/getByLastName")
    public ResponseEntity<Response> getByLastName(@RequestParam String lastName) {
        Response response = new Response();
        try {
            List<Applicant> applicantsList = applicantJpaService.getByLastName(lastName);
            response.setMessage("success");
            response.setPayload(applicantsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/like")
    public ResponseEntity<Response> getApplicants(@RequestParam String value) {
        Response response = new Response();
        try {
            List<Applicant> applicants = applicantJpaService.getApplicants(value);
            response.setMessage("success");
            response.setPayload(applicants);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
