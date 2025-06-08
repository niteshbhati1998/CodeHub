package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RunStatus {
	
	private String executionId;
	private String batchSize;
	private String successCount;
	private String failCount;
	private String executionStep;
	

	public String getExecutionStep() {
		return executionStep;
	}

	public void setExecutionStep(String executionStep) {
		this.executionStep = executionStep;
	}

	public String getExecutionId() {
		return executionId;
	}
	
	@JsonProperty(value="run_id")
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getBatchSize() {
		return batchSize;
	}
	
	@JsonProperty(value="total_file_count")
	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getSuccessCount() {
		return successCount;
	}
	
	@JsonProperty(value="completed")
	public void setSuccessCount(String successCount) {
		this.successCount = successCount;
	}

	public String getFailCount() {
		return failCount;
	}
	
	@JsonProperty(value="failed")
	public void setFailCount(String failCount) {
		this.failCount = failCount;
	}
	
	
}
