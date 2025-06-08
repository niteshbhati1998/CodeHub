package com.cognizant.lineage.upload.service;

import java.util.List;

import com.cognizant.lineage.upload.model.DashboardRequest;

public interface ProjectButtonService {
	
	public void projectNameInsertion(String projectName);
	
	public List<String> gettingProjectNames();

    String getDashboardWarning(DashboardRequest dashboardRequest);
}
