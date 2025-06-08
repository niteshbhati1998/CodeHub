package com.cognizant.lineage.upload.service;

import java.util.HashMap;
import java.util.List;

import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.model.JobStatusList;
import com.cognizant.lineage.upload.model.LineageJobStatusUI;

public interface JobStatusService {
	
	String getJobStatus(Long jobId) throws LineageBusinessException;
	
	List<LineageJobStatusUI> getAllLineageJobs() throws LineageBusinessException;
	
	JobStatusList getExecutionStatus(Long jobId)
			throws LineageBusinessException, LineageRuntimeException;

	void updateJobStatusDetailsWithComplexityString(Long jobId);

	String getDbUploadStatus(HashMap<String, String> jobIdWithTechMap) throws LineageBusinessException;

	int getNoOfOccurrences(String dbUploadStatus);

	public void updateJobStatusDetailsWithScriptLineageString(Long jobId);
}
