package com.cognizant.lineage.upload.model;

public class BarChart {
	
	private String module;
	private long count;
	
	public BarChart(String module, long count) {
		super();
		this.module = module;
		this.count = count;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}

}
