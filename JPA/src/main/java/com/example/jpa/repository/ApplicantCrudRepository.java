package com.example.jpa.repository;

import com.example.jpa.entity.Applicant;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicantCrudRepository extends CrudRepository<Applicant, Long> {
    List<Applicant> findByLastNameOrderByFirstNameAsc(String lastName);

    List<Applicant> findByLastName(String lastName, Sort sort);

    List<Applicant> findByLastNameIgnoreCase(String lastName);

    @Query(value = "select * from staging.applicants_info a where a.first_name like %:val%", nativeQuery = true)
    List<Applicant> getApplicants(@Param("val") String value);

    Optional<Applicant> findByFirstNameAndLastName(String firstName, String lastName);
}
