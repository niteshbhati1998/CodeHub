package com.cognizant.lineage.upload.model;

public class SprintScriptDetails {
	
	private Long scripts = 0L;
	private Long totalLines = 0L;
	
	public SprintScriptDetails(Long scripts, Long totalLines) {
		super();
		this.scripts = scripts;
		this.totalLines = totalLines;
	}
	public Long getScripts() {
		return scripts;
	}
	public void setScripts(Long scripts) {
		this.scripts = scripts;
	}
	public Long getTotalLines() {
		return totalLines;
	}
	public void setTotalLines(Long totalLines) {
		this.totalLines = totalLines;
	}

}
