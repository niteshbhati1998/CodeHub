package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.lineage.dao.entity.QueryComplexityDetails;

@Repository
public interface QueryComplexityDetailsRepository extends JpaRepository<QueryComplexityDetails, String> {
	
	@Query(nativeQuery = true, value = "SELECT * FROM semantic.script_complexity s where s.file_name = :scriptFile and s.project_name = :projectName")
	List<QueryComplexityDetails> findRecordsByFileNameAndProjectName(@Param("scriptFile") String scriptFile, @Param("projectName") String projectName);
	
	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "update semantic.script_complexity set complexity = :complexity where id = :id")
	Integer updateComplexityById(@Param("id") String id, @Param("complexity") String complexity);

	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "DELETE FROM semantic.script_complexity s where s.file_name = :scriptFile and s.project_name = :projectName")
	void deleteRecordsByFileNameAndProjectName(@Param("scriptFile") String scriptFile, @Param("projectName") String projectName);

}
