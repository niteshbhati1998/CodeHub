package com.cognizant.lineage.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.cognizant.lineage.dao.entity.SprintLogStatus;

import jakarta.transaction.Transactional;

public interface SprintLogStatusRepository extends JpaRepository<SprintLogStatus, Long>{
	
	@Modifying
	@Transactional
	void deleteByProjectName(String projectName);
	
	SprintLogStatus findByProjectName(String projectName);
}
