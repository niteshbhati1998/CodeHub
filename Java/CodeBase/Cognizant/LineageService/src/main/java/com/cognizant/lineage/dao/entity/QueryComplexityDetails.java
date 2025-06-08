package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "script_complexity", schema = "semantic")
public class QueryComplexityDetails {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "job_id")
	private Long jobId;

	@Column(name = "project_name")
	private String projectName;

	@Column(name = "technology")
	private String technology;

	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "total_no_of_lines")
	private Integer totalNoOfLine;
	
	@Column(name = "query_count")
	private Integer queryCount;
	
	@Column(name = "insert_count")
	private Integer insertCount;
	
	@Column(name = "update_count")
	private Integer updateCount;
	
	@Column(name = "delete_count")
	private Integer deleteCount;
	
	@Column(name = "create_count")
	private Integer createCount;
	
	@Column(name = "merge_count")
	private Integer mergeCount;
	
	@Column(name = "function_used_count")
	private Integer functionUsedCount;
	
	@Column(name = "join_count")
	private Integer joinCount;
	
	@Column(name = "complexity")
	private String complexity;
	
	@Column(name = "remarks")
	private String remarks;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "if_else_count")
	private Integer if_else_count;
	
	@Column(name = "fun_expressions")
	private String fun_expressions;
	
	@Column(name = "join_types")
	private String join_types;
	
	@Column(name = "select_count")
	private Integer selectCount;

	@Column(name = "aggregator_component_count")
	private Integer aggregatorComponentCount;

	@Column(name = "normalizer_component_count")
	private Integer normalizerComponentCount;

	@Column(name = "procedure_component_count")
	private Integer procedureComponentCount;

	@Column(name = "custom_component_count")
	private Integer customComponentCount;

	@Column(name = "other_component_count")
	private Integer otherComponentCount;

	@Column(name = "table_count")
	private Integer tableCount;

	@Column(name = "condition_count")
	private Integer conditionCount;
}
