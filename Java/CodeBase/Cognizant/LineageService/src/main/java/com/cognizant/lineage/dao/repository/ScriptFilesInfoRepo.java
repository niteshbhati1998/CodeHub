package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cognizant.lineage.dao.entity.ScriptFilesInfo;

public interface ScriptFilesInfoRepo extends JpaRepository<ScriptFilesInfo, Integer> {

	@Query("SELECT distinct sqlText FROM ScriptFilesInfo WHERE jobId= :jobId AND fileName= :fileName")
	List<String> findSqlTextByJobIdAndFileName(@Param("jobId") Integer jobId, @Param("fileName") String fileName);
	
	@Query("SELECT distinct(fileName) FROM ScriptFilesInfo where jobId = :jobId")
	List<String> findFileNamesByJobId(@Param("jobId")Integer jobId);

}
