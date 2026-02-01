package com.example.jpa.controller;

import com.example.jpa.entity.Applicant;
import com.example.jpa.model.Response;
import com.example.jpa.service.ApplicantPagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applicant")
public class ApplicantPagingController {

    @Autowired
    private ApplicantPagingService applicantPagingService;

    @GetMapping("/page")
    public ResponseEntity<Response> getApplicants(@RequestParam int page, @RequestParam int size) {
        Response response = new Response();
        try {
            Page<Applicant> applicantsList = applicantPagingService.getApplicants(page, size);
            response.setMessage("success");
            response.setPayload(applicantsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
