package com.example.jpa.service;

import com.example.jpa.entity.Applicant;
import com.example.jpa.repository.ApplicantJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicantJpaService {

    @Autowired
    private ApplicantJpaRepository applicantJpaRepository;

    public List<Applicant> getByLastName(String lastName) {
        //sort by firstName - approach1
        //return applicantJpaRepository.findByLastNameOrderByFirstNameAsc(lastName);

        //sort by firstName - approach2
        return applicantJpaRepository.findByLastName(lastName, Sort.by("firstName").ascending());

        //ignore-case
        //return applicantJpaRepository.findByLastNameIgnoreCase(lastName);
    }

    public List<Applicant> getApplicants(String value) {
        return applicantJpaRepository.getApplicants(value);
    }
}
