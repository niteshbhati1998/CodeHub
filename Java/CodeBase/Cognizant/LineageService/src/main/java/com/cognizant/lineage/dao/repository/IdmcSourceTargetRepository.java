package com.cognizant.lineage.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.lineage.dao.entity.IdmcSourceTarget;

public interface IdmcSourceTargetRepository extends JpaRepository<IdmcSourceTarget, Long>{
	
}
