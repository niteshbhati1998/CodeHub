package com.cognizant.lineage.dao.entity;

import java.sql.Timestamp;

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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sprint_details_custom_scripts", schema = "presentation")
public class SprintDetailsCustomApplicationBased {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "wave")
	private Integer wave;
	
	@Column(name = "wave_name")
	private String waveName;
	
	@Column(name = "wave_sprint")
	private String waveSprint;
	
	@Column(name = "sprint")
	private Integer sprint;
	
	@Column(name = "sprint_name")
	private String sprintName;
	
	@Column(name = "migration_type")
	private String migrationType;
	
	@Column(name = "startdate")
	private Timestamp startDate;
	
	@Column(name = "enddate")
	private Timestamp endDate;
	
	@Column(name = "duration")
	private String duration;
	
	@Column(name = "size")
	private String size;

	@Column(name = "complexity")
	private String complexity;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "unit")
	private String unit;
	
	@Column(name = "value")
	private String value;
}
