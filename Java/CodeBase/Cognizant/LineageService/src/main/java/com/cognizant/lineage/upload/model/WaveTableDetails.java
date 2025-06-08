package com.cognizant.lineage.upload.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WaveTableDetails {
	
	private String functionalModule;
	private String applicationType;
	private String type;
	private String tech;
	private List<Wave> waves; 
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Wave> getWaves() {
		return waves;
	}
	public void setWaves(List<Wave> waves) {
		this.waves = waves;
	}
	public String getApplicationType() {
		return applicationType;
	}
	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}
	public String getTech() {
		return tech;
	}
	public void setTech(String tech) {
		this.tech = tech;
	}
	public String getFunctionalModule() {
		return functionalModule;
	}
	public void setFunctionalModule(String functionalModule) {
		this.functionalModule = functionalModule;
	}
	
}
