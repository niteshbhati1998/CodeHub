package com.example.jpa.service;

import com.example.jpa.entity.Applicant;
import com.example.jpa.repository.ApplicantCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicantCrudService {

    @Autowired
    private ApplicantCrudRepository applicantCrudRepository;

    public Applicant saveApplicant(Applicant applicant) {
        return applicantCrudRepository.save(applicant);
    }

    public List<Applicant> getAllApplicants() {
        List<Applicant> applicant = new ArrayList<>();
        Iterable<Applicant> itr = applicantCrudRepository.findAll();
        itr.forEach(applicant::add);
        return applicant;
    }

    public Applicant updateApplicant(Applicant applicant) {
        Optional<Applicant> getApplicant = applicantCrudRepository.findByFirstNameAndLastName(applicant.getFirstName(), applicant.getLastName());

        if(getApplicant.isPresent()) {
            Applicant app = getApplicant.get();
            app.setEmail(applicant.getEmail());
            return applicantCrudRepository.save(app);
        } else {
            throw new RuntimeException("Applicant not found");
        }
    }
}
