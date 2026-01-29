package com.example.jpa.repository;

import com.example.jpa.entity.Applicant;
import org.springframework.data.repository.CrudRepository;

public interface ApplicantRepository extends CrudRepository<Applicant, Long> {
}
