package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.IdmcLinks;

public interface IdmcLinksRepository extends JpaRepository<IdmcLinks, Long>{

	@Query(value = "select id, project_name, job_id, from_transformation_class, from_transformation_id, to_transformation_class, to_transformation_id, mapping_name, filename from idmc_links " + 
			"where from_transformation_class = :fromTransformationClass and job_id = :jobId and filename = :fileName", nativeQuery = true)
	List<IdmcLinks> getLinksInfoBasedOnClassId(int fromTransformationClass, int jobId, String fileName);
	
	@Query(value = "select id, project_name, job_id, from_transformation_class, from_transformation_id, to_transformation_class, to_transformation_id, mapping_name, filename from idmc_links " + 
			"where from_transformation_class = :fromTransformationClass and from_transformation_id = :fromTransformationId and job_id = :jobId and filename = :fileName", nativeQuery = true)
	List<IdmcLinks> getLinksInfoBasedOnClassIdAndId(int fromTransformationClass, int fromTransformationId, int jobId, String fileName);
}
