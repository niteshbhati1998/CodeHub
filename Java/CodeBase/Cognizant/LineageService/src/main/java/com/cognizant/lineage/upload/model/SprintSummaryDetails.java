package com.cognizant.lineage.upload.model;

import java.util.List;

import lombok.Data;

@Data
public class SprintSummaryDetails {
	
	private String functionalModule;
	private String database;
	private List<SprintDetails> sprintDetails; 

}
