package com.cognizant.lineage.upload.model;

public class PiChartDetails {
	
	private String technology;
	private Long script = 0L;

	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public Long getScript() {
		return script;
	}
	public void setScript(Long script) {
		this.script = script;
	}
	@Override
	public String toString() {
		return "PiChartDetails [technology=" + technology + ", script=" + script + "]";
	}
}