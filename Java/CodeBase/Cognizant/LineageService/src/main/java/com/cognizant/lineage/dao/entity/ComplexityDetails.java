package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "script_calculation_details", schema = "semantic")
public class ComplexityDetails {
	
	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "complexity")
	private String complexity;
	
	@Column(name = "function_used_count")
	private Integer functionUsedCount;
	
	@Column(name = "query_count")
	private Integer queryCount;
	
	@Column(name = "total_no_of_lines")
	private Integer totalNoOfLines;
	
	@Column(name = "update_count")
	private Integer updateQueryCount;
	
	@Column(name = "delete_count")
	private Integer deleteQueryCount;
	
	@Column(name = "insert_count")
	private Integer insertQueryCount;
	
	@Column(name = "create_count")
	private Integer createQueryCount;
	
	@Column(name = "merge_count")
	private Integer mergeQueryCount;
	
	@Column(name = "join_count")
	private Integer joinCount;
	
	@Column(name = "technology")
	private String technology;
	
	@Column(name = "if_else_count")
	private Integer if_else_count;

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
