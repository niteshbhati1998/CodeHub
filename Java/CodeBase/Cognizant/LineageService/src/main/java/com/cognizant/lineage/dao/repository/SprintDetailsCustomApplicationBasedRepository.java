package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.SprintDetailsCustomApplicationBased;

import jakarta.transaction.Transactional;

public interface SprintDetailsCustomApplicationBasedRepository extends CrudRepository<SprintDetailsCustomApplicationBased,Long> {
	
	@Modifying
	@Transactional
	void deleteByProjectNameAndWave(String projectName, Integer wave);
	
	@Query(value = "select distinct s.wave from presentation.sprint_details_custom_scripts s where s.project_name = :projectName order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveList(String projectName);
	
	@Query(value = "select distinct s.sprint from presentation.sprint_details_custom_scripts s where s.project_name = :projectName and s.wave = :wave order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintList(String projectName, Integer wave);
	
	@Query(value = "select * from presentation.sprint_details_custom_scripts s where s.project_name = :projectName and s.wave = :wave and s.sprint = :sprint" , nativeQuery = true)
	List<SprintDetailsCustomApplicationBased> getDataForEachSprint(String projectName, Integer wave, Integer sprint);
}