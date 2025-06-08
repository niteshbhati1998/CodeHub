package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "idmc_source_target", schema = "semantic")
public class IdmcSourceTarget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "job_id")
	private int jobId;
	
	@Column(name = "source")
	private String source;
	
	@Column(name = "target")
	private String target;
	
	@Column(name = "source_transformation_name")
	private String sourceTransformationName;
		
	@Column(name = "target_transformation_name")
	private String targetTransformationName;
	
	@Column(name = "mapping_name")
	private String mappingName;
	
	@Column(name = "filename")
	private String fileName;
}
