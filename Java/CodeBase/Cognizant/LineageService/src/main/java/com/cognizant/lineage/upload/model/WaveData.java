package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class WaveData{
	
	private String module;
	private Integer wave;
	private String technology;
	private String complexity;
	private Long countOfComplexity;
	public WaveData(String module, Integer wave, String technology, String complexity, Long countOfComplexity) {
		super();
		this.module = module;
		this.wave = wave;
		this.technology = technology;
		this.complexity = complexity;
		this.countOfComplexity = countOfComplexity;
	}	
}
