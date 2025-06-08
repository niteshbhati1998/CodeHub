package com.cognizant.lineage.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.IdmcTransformations;

public interface IdmcTransformationsRepository extends JpaRepository<IdmcTransformations, Long>{

	@Query(value = "select id, project_name, job_id, class_val, id_val, table_name, transformation_name, custom_query, pre_sql, post_sql, sql_override, mapping_name, filename from idmc_transformations " + 
			"where class_val = :classId and id_val = :id and job_id = :jobId and filename = :fileName", nativeQuery = true)
	IdmcTransformations getTransformationsInfoBasedOnClassIdAndId(int classId, int id, int jobId, String fileName);
}
