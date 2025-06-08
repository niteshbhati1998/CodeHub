package com.cognizant.lineage.upload.model;

public class SprintQueryCount {
	
	private String technology;
	private String module;
	private Long count;
	
	public SprintQueryCount() {
		super();
	}
	public SprintQueryCount(String technology, String module, Long count) {
		super();
		this.technology = technology;
		this.module = module;
		this.count = count;
	}
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
}
