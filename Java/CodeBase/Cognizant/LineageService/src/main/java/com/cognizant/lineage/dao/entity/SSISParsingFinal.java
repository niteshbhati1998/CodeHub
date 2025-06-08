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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ssis_parsing_final", schema = "semantic")
public class SSISParsingFinal {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;
	
	@Column(name = "job_id")
	private Long jobId;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "source")
	private String source;
	
	@Column(name = "source_transformation_type")
	private String sourceTransformationType;
	
	@Column(name = "target")
	private String target;
	
	@Column(name = "target_transformation_type")
	private String targetTransformationType;
	
	@Column(name = "sql_query")
	private String sqlQuery;
}
