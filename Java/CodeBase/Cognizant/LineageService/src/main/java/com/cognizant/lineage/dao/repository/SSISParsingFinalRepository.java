package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.SSISParsingFinal;

public interface SSISParsingFinalRepository extends JpaRepository<SSISParsingFinal, Long>{
	
	@Query(value = "select distinct file_name from semantic.ssis_parsing_final where job_id = :jobId and project_name = :projectName", nativeQuery = true)
	List<String> getFilenamesList(Long jobId, String projectName);
	
	@Query(value = "select distinct sql_query from semantic.ssis_parsing_final where job_id = :jobId and project_name = :projectName and file_name = :fileName", nativeQuery = true)
	List<String> getSqlQueryBasedOnFilename(Long jobId, String projectName, String fileName);
}
