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
    public ResponseEntity<Response<Applicant>> saveApplicant(@Valid @RequestBody Applicant applicant) {
        Applicant savedApplicant = applicantService.saveApplicant(applicant);
        Response<Applicant> response = new Response<>("success", savedApplicant);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<Response<List<Applicant>>> getApplicants() {
        List<Applicant> applicantsList = applicantService.getApplicants();
        Response<List<Applicant>> response = new Response<>("success", applicantsList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getByLastName")
    public ResponseEntity<Response<List<Applicant>>> getByLastName(@RequestParam String lastName) {
        List<Applicant> applicantsList = applicantService.getByLastName(lastName);
        Response<List<Applicant>> response = new Response<>("success", applicantsList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getByValue")
    public ResponseEntity<Response<List<Applicant>>> getByValue(@RequestParam String value) {
        List<Applicant> applicants = applicantService.getByValue(value);
        Response<List<Applicant>> response = new Response<>("success", applicants);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/updateEmail")
    public ResponseEntity<Response<Applicant>> updateApplicant(@Valid @RequestBody Applicant applicant) {
        Applicant updatedApplicant = applicantService.updateApplicant(applicant);
        Response<Applicant> response = new Response<>("success", updatedApplicant);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getByPage")
    public ResponseEntity<Response<Page<Applicant>>> getByPage(@RequestParam int page, @RequestParam int size) {
        Page<Applicant> applicantsList = applicantService.getByPage(page, size);
        Response<Page<Applicant>> response = new Response<>("success", applicantsList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
