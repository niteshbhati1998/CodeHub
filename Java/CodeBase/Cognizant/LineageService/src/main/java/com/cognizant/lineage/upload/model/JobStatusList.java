package com.cognizant.lineage.upload.model;

import java.util.List;

public class JobStatusList {
	
	private Long jobId;
	
	private List<JobStatus> jobStatusList;

	private String statusSummary;

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public List<JobStatus> getJobStatusList() {
		return jobStatusList;
	}

	public void setJobStatusList(List<JobStatus> jobStatusList) {
		this.jobStatusList = jobStatusList;
	}

	public String getStatusSummary() {
		return statusSummary;
	}

	public void setStatusSummary(String statusSummary) {
		this.statusSummary = statusSummary;
	}
}
