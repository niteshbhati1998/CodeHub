package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.dao.entity.OdiDetails;

@Repository
public interface ODIComplexityRepository extends JpaRepository<OdiDetails, Integer> {
	
	@Query("SELECT distinct(fileName) FROM OdiDetails s where s.jobId = :jobId")
	List<String> findFileNamesByJobId(@Param("jobId")Integer jobId);
	
	@Query(value= "select new_sqltext from semantic.odi_details where job_id = :jobId and "
			+ "fileName = :fileName and (upper(new_sqltext) like 'INS %' or upper(new_sqltext) like 'INSERT %' or upper(new_sqltext) like 'UPD %' OR upper(new_sqltext) like 'UPDATE %'\r\n" + 
			"or upper(new_sqltext) like 'DEL %' or upper(new_sqltext) like 'DELETE %' or upper(new_sqltext) like 'CREATE %' or upper(new_sqltext) like 'MERGE %');", nativeQuery = true)
	List<String> findNewSQLTextByJobId(@Param("jobId")Integer jobId, @Param("fileName")String fileName);
	
	@Query(value= "select function_list from semantic.function_list", nativeQuery = true)
	List<String> getFunList();
}
