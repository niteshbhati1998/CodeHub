package com.cognizant.lineage.upload.model;

public class JobStatus {
	
	private Integer step;
	
	private String stepName;
	
	private String status;
	
	private String message;

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "JobStatus [step=" + step + ", stepName=" + stepName + ", status=" + status + ", message=" + message
				+ "]";
	}

	
}
