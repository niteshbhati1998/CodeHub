package com.cognizant.lineage.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.InformaticaParamDetails;

import jakarta.transaction.Transactional;

public interface InformaticaParamDetailsRepository extends JpaRepository<InformaticaParamDetails, Long> {

	@Modifying
	@Transactional
	@Query(value = "truncate table semantic.informatica_param_details", nativeQuery = true)
	public void truncateInformaticaParamDetails();
}
