package com.cognizant.lineage.upload.model;

import java.util.List;

import lombok.Data;

@Data
public class SprintSummaryData {
	
	private String projectName;
	private List<String> module;
	private List<String> technology;
}
