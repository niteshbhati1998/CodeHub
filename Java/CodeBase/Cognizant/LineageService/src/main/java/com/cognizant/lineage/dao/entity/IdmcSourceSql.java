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
@Table(name = "idmc_source_sql", schema = "semantic")
public class IdmcSourceSql {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "job_id")
	private int jobId;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "folder_name")
	private String folderName;
	
	@Column(name = "mapping_name")
	private String mappingName;
	
	@Column(name = "query_type")
	private String queryType;
	
	@Column(name = "sql")
	private String sql;
	
	@Column(name = "source_type")
	private String sourceType;
	
	@Column(name = "target")
	private String target;
	
	@Column(name = "from_component")
	private String fromComponent;
	
	@Column(name = "to_component")
	private String toComponent;
}
