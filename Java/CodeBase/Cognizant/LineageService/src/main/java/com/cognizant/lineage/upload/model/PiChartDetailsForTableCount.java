package com.cognizant.lineage.upload.model;

public class PiChartDetailsForTableCount {
	
	 private String technology;
	 private String type;
	 private Long count ;
	 
	@Override
	public String toString() {
		return "PiChartDetailsForTableCount [type=" + type + ", technology=" + technology + ", count=" + count + "]";
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	 

}
