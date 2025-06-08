package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.SSISParsing;

public interface SSISParsingRepository extends JpaRepository<SSISParsing, Long>{
	
	@Query(value = "select distinct workflow_name from semantic.ssis_parsing where job_id = :jobId and project_name = :projectName", nativeQuery = true)
	List<String> getWorkflowNameList(Long jobId, String projectName);
	
	@Query(value = "select distinct mapping_name from semantic.ssis_parsing where job_id = :jobId and project_name = :projectName and workflow_name = :workflowName", nativeQuery = true)
	List<String> getMappingNameList(Long jobId, String projectName, String workflowName);
	
	@Query(value = "select distinct source from semantic.ssis_parsing where job_id = :jobId and project_name = :projectName and workflow_name = :workflowName and mapping_name = :mappingName and (lower(source_transformation_type) like '%source%' or lower(source_transformation_type) like '%lookup%')", nativeQuery = true)
	List<String> getSourceList(Long jobId, String projectName, String workflowName, String mappingName);
	
	List<SSISParsing> findDistinctByJobIdAndProjectNameAndWorkflowNameAndMappingNameAndSource(Long jobId, String projectName, String workflowName, String mappingName, String source);
}
