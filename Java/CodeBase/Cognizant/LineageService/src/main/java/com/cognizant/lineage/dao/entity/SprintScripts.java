package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "sprint_scripts", schema = "presentation")
@Entity
public class SprintScripts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "script_id")
	private Long scriptId;

	@Column(name = "project_name")
	private String projectName;

	@Column(name = "technology")
	private String technology;

	@Column(name = "script_name")
	private String scriptName;

	@Column(name = "module")
	private String module;

	@Column(name = "complexity")
	private String complexity;

	@Column(name = "no_of_lines")
	private Integer noOfLines;
	
	@Column(name = "sprint")
	private Integer wave;

	public Long getScriptId() {
		return scriptId;
	}

	public void setScriptId(Long scriptId) {
		this.scriptId = scriptId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getComplexity() {
		return complexity;
	}

	public void setComplexity(String complexity) {
		this.complexity = complexity;
	}

	public Integer getNoOfLines() {
		return noOfLines;
	}

	public void setNoOfLines(Integer noOfLines) {
		this.noOfLines = noOfLines;
	}

	public Integer getWave() {
		return wave;
	}

	public void setWave(Integer wave) {
		this.wave = wave;
	}

}
