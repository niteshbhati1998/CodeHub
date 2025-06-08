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
@Table(name = "idmc_links", schema = "semantic")
public class IdmcLinks {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "job_id")
	private int jobId;
	
	@Column(name = "from_transformation_class")
	private int fromTransformationClass;
	
	@Column(name = "from_transformation_id")
	private int fromTransformationId;
	
	@Column(name = "to_transformation_class")
	private int toTransformationClass;
	
	@Column(name = "to_transformation_id")
	private int toTransformationId;
	
	@Column(name = "mapping_name")
	private String mappingName;
	
	@Column(name = "filename")
	private String fileName;
}
