package com.example.jpa.repository;

import com.example.jpa.entity.Applicant;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ApplicantPagingRepository extends PagingAndSortingRepository<Applicant, Long> {
}
