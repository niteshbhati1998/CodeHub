package com.example.jpa.service;

import com.example.jpa.entity.Applicant;
import com.example.jpa.repository.ApplicantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicantService {

    @Autowired
    private ApplicantRepository applicantRepository;

    public Applicant saveApplicant(Applicant applicant) {
        return applicantRepository.save(applicant);
    }

    public List<Applicant> getAllApplicants() {
        List<Applicant> applicant = new ArrayList<>();
        Iterable<Applicant> itr = applicantRepository.findAll();
        itr.forEach(applicant::add);
        return applicant;
    }
}
