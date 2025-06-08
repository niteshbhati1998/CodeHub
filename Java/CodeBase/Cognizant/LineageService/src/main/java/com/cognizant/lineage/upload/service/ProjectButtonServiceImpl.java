package com.cognizant.lineage.upload.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.dao.ProjectButtonDao;
import com.cognizant.lineage.upload.model.DashboardRequest;

@Service
public class ProjectButtonServiceImpl implements ProjectButtonService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectButtonServiceImpl.class);
	
	@Autowired
	private ProjectButtonDao projectButtonDao;

	@Override
	public void projectNameInsertion(String projectName) {
		
		try {
			projectButtonDao.projectNameInsertion(projectName);
		} catch (Exception e) {
			LOGGER.error("Exception occurred in projectNameInsertion: " + e.getMessage());
		}
	}

	@Override
	public List<String> gettingProjectNames() {
		List<String> projectLists;
		List<String> returnProjectLists = new ArrayList<>();
		try {
			projectLists = projectButtonDao.gettingProjectNames();
			
			for (String prj: projectLists) {
				prj = prj.replaceAll("[^a-zA-Z0-9_ ]", "");
				returnProjectLists.add(prj);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred in gettingProjectNames service " + e.getMessage());
		}
		return returnProjectLists;
	}

	@Override
	public String getDashboardWarning(DashboardRequest dashboardRequest) {
		Integer count;
		if ("dataAssetMapping".equalsIgnoreCase(dashboardRequest.getDashboardName()) ||
				"tableLineage".equalsIgnoreCase(dashboardRequest.getDashboardName()) ||
				"lineageMetadata".equalsIgnoreCase(dashboardRequest.getDashboardName())) {
			count = projectButtonDao.getCountOfRecordsInEdgesForProject(dashboardRequest.getProjectName());
			return checkCountAndReturnResponse(count, GeneralConstants.DASHBOARD_WARNING_WITH_ETL_BI_PAGE);
		} else if ("hotSpot".equalsIgnoreCase(dashboardRequest.getDashboardName())) {
			count = projectButtonDao.getCountOfRecordsInNodesForProject(dashboardRequest.getProjectName());
			return checkCountAndReturnResponse(count, GeneralConstants.DASHBOARD_WARNING_WITH_ETL_BI_PAGE);
		} else if ("businessLineage".equalsIgnoreCase(dashboardRequest.getDashboardName())) {
			count = projectButtonDao.getCountOfRecordsInModuleWiseObjectsForProject(dashboardRequest.getProjectName());
			return checkCountAndReturnResponse(count, GeneralConstants.DASHBOARD_WARNING_WITH_DATA_ASSET_MAPPING_PAGE);
		} else if ("migrationWavePlan".equalsIgnoreCase(dashboardRequest.getDashboardName()) ||
				"dataMigrationWavePlan".equalsIgnoreCase(dashboardRequest.getDashboardName()) ||
				"applicationMigrationWavePlan".equalsIgnoreCase(dashboardRequest.getDashboardName()) ||
				"wavePlanDetails".equalsIgnoreCase(dashboardRequest.getDashboardName())) {
			count = projectButtonDao.getCountOfRecordsInSprintObjectsForProject(dashboardRequest.getProjectName());
			return checkCountAndReturnResponse(count, GeneralConstants.DASHBOARD_WARNING_WITH_WAVE_PLAN_PAGE);
		} else if("sprintpage".equalsIgnoreCase(dashboardRequest.getDashboardName())) {
			count = projectButtonDao.getCountOfRecordsInSprintDetailsForProject(dashboardRequest.getProjectName());
			return checkCountAndReturnResponse(count, GeneralConstants.DASHBOARD_WARNING_WITH_SPRINT_DETAILS_PAGE);
		}
		return "Success";
	}

	private String checkCountAndReturnResponse(Integer count, String dashboardWarningMessage) {
		if (count > 0) {
			return "Success";
		}
		return dashboardWarningMessage;
	}

}
