package com.example.jpa.service;

import com.example.jpa.entity.Applicant;
import com.example.jpa.exception.ApplicantNotFoundException;
import com.example.jpa.repository.ApplicantCrudRepository;
import com.example.jpa.repository.ApplicantJpaRepository;
import com.example.jpa.repository.ApplicantPagingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicantService {

    @Autowired
    private ApplicantCrudRepository applicantCrudRepository;

    @Autowired
    private ApplicantJpaRepository applicantJpaRepository;

    @Autowired
    private ApplicantPagingRepository applicantPagingRepository;

    public Applicant saveApplicant(Applicant applicant) {
        return applicantCrudRepository.save(applicant);
    }

    public List<Applicant> getApplicants() {
        List<Applicant> applicant = new ArrayList<>();
        Iterable<Applicant> itr = applicantCrudRepository.findAll();
        itr.forEach(applicant::add);
        return applicant;
    }

    public List<Applicant> getByLastName(String lastName) {
        //sort by firstName
        //return applicantCrudRepository.findByLastNameOrderByFirstNameAsc(lastName);
        return applicantCrudRepository.findByLastName(lastName, Sort.by("firstName").ascending());

        //ignore-case
        //return applicantCrudRepository.findByLastNameIgnoreCase(lastName);
    }

    public List<Applicant> getByValue(String value) {
        return applicantCrudRepository.getApplicants(value).filter(list -> !list.isEmpty()).orElseThrow(() -> new ApplicantNotFoundException("Applicant not found"));
    }

    public Applicant updateApplicant(Applicant applicant) {
        Optional<Applicant> getApplicant = applicantCrudRepository.findByFirstNameAndLastName(applicant.getFirstName(), applicant.getLastName());

        if (getApplicant.isPresent()) {
            Applicant app = getApplicant.get();
            app.setEmail(applicant.getEmail());
            return applicantCrudRepository.save(app);
        } else {
            throw new ApplicantNotFoundException("Applicant not found");
        }
    }

    public Page<Applicant> getByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending().and(Sort.by("lastName").descending()));
        return applicantPagingRepository.findAll(pageable);
    }
}
