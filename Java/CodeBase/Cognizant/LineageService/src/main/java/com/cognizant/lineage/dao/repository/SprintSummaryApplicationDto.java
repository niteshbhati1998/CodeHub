package com.cognizant.lineage.dao.repository;

public interface SprintSummaryApplicationDto {
	
	Integer getSprint();
	String getTechnology();
	String getComplexity();
	Integer getCount();
    String getModule();
}
