package com.cognizant.lineage.dao.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.dao.entity.LineageJob;

import jakarta.transaction.Transactional;

@Repository
public interface LineageJobRepository extends CrudRepository<LineageJob, Long>{

	public List<LineageJob> findAllByOrderByJobIdDesc();
	
	List<LineageJob> findByJobId(Long jobId);
	
	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "update semantic.lineage_job set end_time = :endTime where job_id = :jobId")
	void updateEndTime(Date endTime, Long jobId); 
	
	@Query(nativeQuery = true, value = "select coalesce(max(job_id)+1, 1) from semantic.lineage_job")
	Long getJobIdByMax();
}
