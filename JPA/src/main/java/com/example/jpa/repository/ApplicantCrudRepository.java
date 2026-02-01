package com.example.jpa.repository;

import com.example.jpa.entity.Applicant;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicantCrudRepository extends CrudRepository<Applicant, Long> {
    Optional<Applicant> findByFirstNameAndLastName(String firstName, String lastName);
}
