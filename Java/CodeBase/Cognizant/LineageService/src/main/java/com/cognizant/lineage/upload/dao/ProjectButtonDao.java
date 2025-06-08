package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.QueryConstant;

@Repository
public class ProjectButtonDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectButtonDao.class);
	
	@Autowired
	@Qualifier("presentationJdbcTemplate")
	private JdbcTemplate presentationJdbcTemplate;
	
	public void projectNameInsertion(String projectName) {
		try {
			Object[] param = { projectName};
			presentationJdbcTemplate.update(QueryConstant.INSERT_PROJECT_NAMES, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in projectNameInsertion dao" + ex.getMessage());
		}
	}


	public List<String> gettingProjectNames() {
		List<String> projectLists = new ArrayList<>();
		try {
			projectLists = presentationJdbcTemplate.queryForList(QueryConstant.GET_PROJECT_NAMES, String.class);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in gettingProjectNames dao " + ex.getMessage());
		}
		return projectLists;
	}

	public Integer getCountOfRecordsInEdgesForProject(String projectName) {
		Integer result = 0;
		Object[] param = {projectName};
		String query = "select count(*) from presentation.edges where project_name = ?";
		try {
			result = presentationJdbcTemplate.queryForObject(query, Integer.class, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getCountOfRecordsInEdgesForProject dao " + ex.getMessage());
		}
		return result;
	}

	public Integer getCountOfRecordsInNodesForProject(String projectName) {
		Integer result = 0;
		Object[] param = {projectName};
		String query = "select count(*) from presentation.nodes where project_name = ?";
		try {
			result = presentationJdbcTemplate.queryForObject(query, Integer.class, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getCountOfRecordsInNodesForProject dao " + ex.getMessage());
		}
		return result;
	}

	public Integer getCountOfRecordsInSprintObjectsForProject(String projectName) {
		Integer result = 0;
		Object[] param = {projectName};
		String query = "select count(*) from presentation.sprint_objects where project_name = ?";
		try {
			result = presentationJdbcTemplate.queryForObject(query, Integer.class, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getCountOfRecordsInSprintObjectsForProject dao " + ex.getMessage());
		}
		return result;
	}
	
	public Integer getCountOfRecordsInSprintDetailsForProject(String projectName) {
		Integer result = 0;
		Object[] param = {projectName.trim().toUpperCase()};
		String query = "select count(*) from presentation.sprint_details where upper(trim(project_name)) = ?";
		try {
			result = presentationJdbcTemplate.queryForObject(query, Integer.class, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getCountOfRecordsInSprintDetailsForProject dao " + ex.getMessage());
		}
		return result;
	}

	public Integer getCountOfRecordsInModuleWiseObjectsForProject(String projectName) {
		Integer result = 0;
		Object[] param = {projectName};
		String query = "select count(*) from semantic.module_wise_objects_name where project_name = ?";
		try {
			result = presentationJdbcTemplate.queryForObject(query, Integer.class, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getCountOfRecordsInModuleWiseObjectsForProject dao " + ex.getMessage());
		}
		return result;
	}
}
