package com.cognizant.lineage.upload.model;

public class PiChartDetailsForObjectSize {
	
	private String technology;
	private Long count;
	
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
	@Override
	public String toString() {
		return "PiChartDetailsForObjectSize [technology=" + technology + ", count=" + count + "]";
	}	

}
