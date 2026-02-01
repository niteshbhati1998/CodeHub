package com.example.jpa.service;

import com.example.jpa.entity.Applicant;
import com.example.jpa.repository.ApplicantPagingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ApplicantPagingService {

    @Autowired
    private ApplicantPagingRepository applicantPagingRepository;

    public Page<Applicant> getApplicants(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending().and(Sort.by("lastName").descending()));
        return applicantPagingRepository.findAll(pageable);
    }
}
