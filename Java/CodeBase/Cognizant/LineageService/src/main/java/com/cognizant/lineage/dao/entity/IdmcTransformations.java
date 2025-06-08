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
@Table(name = "idmc_transformations", schema = "semantic")
public class IdmcTransformations {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "job_id")
	private int jobId;
	
	@Column(name = "class_val")
	private int classVal;
	
	@Column(name = "id_val")
	private int idVal;
	
	@Column(name = "table_name")
	private String tableName;
	
	@Column(name = "transformation_name")
	private String transformationName;
	
	@Column(name = "custom_query")
	private String customQuery;
	
	@Column(name = "pre_sql")
	private String preSql;
	
	@Column(name = "post_sql")
	private String postSql;
	
	@Column(name = "sql_override")
	private String sqlOverride;
	
	@Column(name = "mapping_name")
	private String mappingName;
	
	@Column(name = "filename")
	private String fileName;
}