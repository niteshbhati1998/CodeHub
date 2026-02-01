package com.example.jpa.repository;

import com.example.jpa.entity.Applicant;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicantJpaRepository extends JpaRepository<Applicant, Long> {

    List<Applicant> findByLastNameOrderByFirstNameAsc(String lastName);

    List<Applicant> findByLastName(String lastName, Sort sort);

    List<Applicant> findByLastNameIgnoreCase(String lastName);

    @Query(value = "select * from staging.applicants_info a where a.first_name like %:val%", nativeQuery = true)
    List<Applicant> getApplicants(@Param("val") String value);
}
