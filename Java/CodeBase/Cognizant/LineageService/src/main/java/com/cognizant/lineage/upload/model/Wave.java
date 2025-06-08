package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Wave {
	
	@JsonProperty("Wave")
	private String wave;
	@JsonProperty("Simple")
	private long simple;
	@JsonProperty("Medium")
	private long medium;
	@JsonProperty("Complex")
	private long complex;
	@JsonProperty("VeryComplex")
	private long veryComplex;
	@JsonProperty("Total")
	private long total;
	
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public long getSimple() {
		return simple;
	}
	public void setSimple(long simple) {
		this.simple = simple;
	}
	public long getMedium() {
		return medium;
	}
	public void setMedium(long medium) {
		this.medium = medium;
	}
	public long getComplex() {
		return complex;
	}
	public void setComplex(long complex) {
		this.complex = complex;
	}
	public String getWave() {
		return wave;
	}
	public void setWave(String wave) {
		this.wave = wave;
	}
	public long getVeryComplex() {
		return veryComplex;
	}
	public void setVeryComplex(long veryComplex) {
		this.veryComplex = veryComplex;
	}
	
	
}
