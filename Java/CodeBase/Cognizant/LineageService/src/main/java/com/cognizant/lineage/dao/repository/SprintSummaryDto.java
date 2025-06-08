package com.cognizant.lineage.dao.repository;

public interface SprintSummaryDto {
	
	Integer getSprint();
	String getModule();
	String getTechnology();
	String getObjectName();
	String getComplexity();
	Integer getTotalCount();
	Integer getTotalSize();
}
