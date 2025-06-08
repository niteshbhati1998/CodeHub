package com.cognizant.lineage.dao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.upload.constants.GeneralConstants;

import jakarta.transaction.Transactional;

@Repository
public interface LineageJobStatusRepository extends CrudRepository<LineageJobStatus, Long>{
	
	@Query (value="select s from LineageJobStatus s where s.jobId= :jobId order by s.stepNo")
	 List<LineageJobStatus> findAllByJobId(@Param("jobId") Long jobId);
	
	@Query (value="select s from LineageJobStatus s where s.jobId= (select max(l.jobId) from LineageJobStatus l) order by s.stepNo")
	 List<LineageJobStatus> findAllByMaxJobId();
	
	@Query (nativeQuery = true, value="select * from lineage_job_status s where s.job_id= :jobId order by s.step_no desc limit 1")
	 Optional<LineageJobStatus> findJobIdOrderByDescLimitTo1(Long jobId);

	@Query(nativeQuery = true, value = "select * from semantic.lineage_job_status where job_id= :jobId and job_status_details like '%" +
			GeneralConstants.PROCESSED_FOR_PROJECT_STRING + "%' order by id asc")
	List<LineageJobStatus> getJobStatusWithLikeString(@Param("jobId") Long jobId);
	
	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "update semantic.lineage_job_status set no_of_file_processed = :noOfFileProcessed, job_status_details = :jobStatusDetails, status = :status where job_id = :jobId and step_no = :stepNo")
	void updateNoOfFilesProcessedAndJobStatusDetails(int noOfFileProcessed, String jobStatusDetails, String status, Long jobId, int stepNo); 
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(nativeQuery = true, value = "update semantic.lineage_job_status set no_of_file_processed = :processedFileCount, no_of_query_parsing_issue = :noOfQueryParsingIssue, no_of_query_service_issue = :noOfQueryServiceIssue, no_of_query_skipped = :noOfQuerySkipped, status = :status, job_status_details = :jobStatusDetails  where job_id = :jobId and step_no = :stepNo")
	void updateFileParsingInfo(int processedFileCount, int noOfQueryParsingIssue, int noOfQueryServiceIssue, int noOfQuerySkipped, String status, String jobStatusDetails, Long jobId, int stepNo); 
}
