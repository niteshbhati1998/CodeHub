package com.example.jpa.repository;

import com.example.jpa.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantJpaRepository extends JpaRepository<Applicant, Long> {

}
