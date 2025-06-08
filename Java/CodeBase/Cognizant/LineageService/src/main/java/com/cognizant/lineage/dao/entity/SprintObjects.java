package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "sprint_objects", schema = "presentation")
@Entity
public class SprintObjects {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "island_id")
    private Integer islandId;

    @Column(name = "level")
    private Integer level;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "module")
    private String module;

    @Column(name = "complexity")
    private String complexity;
    
    @Column(name = "object_size")
    private Integer objectSize;
    
    @Column(name = "sprint")
	private Integer wave;
    
    @Column(name = "technology")
	private String technology;
    
    @Column(name = "object_type")
	private String objectType;
    
    @Column(name = "script_name")
	private String scriptName;

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getIslandId() {
		return islandId;
	}

	public void setIslandId(Integer islandId) {
		this.islandId = islandId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
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

	public Integer getObjectSize() {
		return objectSize;
	}

	public void setObjectSize(Integer objectSize) {
		this.objectSize = objectSize;
	}

	public Integer getWave() {
		return wave;
	}

	public void setWave(Integer wave) {
		this.wave = wave;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

}
