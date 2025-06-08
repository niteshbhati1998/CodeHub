package com.cognizant.lineage.upload.model;

public class Datastaging {

	private String item;
	private String identifier;
	private String inputPin;
	private String outputPin;
	private String parnter;
	private String lookup;
	private String jobName;
	
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getInputPin() {
		return inputPin;
	}
	public void setInputPin(String inputPin) {
		this.inputPin = inputPin;
	}
	public String getOutputPin() {
		return outputPin;
	}
	public void setOutputPin(String outputPin) {
		this.outputPin = outputPin;
	}
	public String getParnter() {
		return parnter;
	}
	public void setParnter(String parnter) {
		this.parnter = parnter;
	}
	public String getLookup() {
		return lookup;
	}
	public void setLookup(String lookup) {
		this.lookup = lookup;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	@Override
	public String toString() {
		return "Datastaging [item=" + item + ", identifier=" + identifier + ", inputPin=" + inputPin + ", outputPin="
				+ outputPin + ", parnter=" + parnter + ", jobName=" + jobName + "]";
	}
		
}

