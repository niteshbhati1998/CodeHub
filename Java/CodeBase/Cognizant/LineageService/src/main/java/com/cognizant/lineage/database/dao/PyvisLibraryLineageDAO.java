package com.cognizant.lineage.database.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.database.constants.PyvisLibraryQueryConstant;

@Repository
public class PyvisLibraryLineageDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PyvisLibraryLineageDAO.class);
	  
	@Autowired
	@Qualifier("presentationJdbcTemplate")
	private JdbcTemplate presentationJdbcTemplate;

	public List<String> getFilterDropdownList(String projectName, String type) {
		List<String> filterDropdownList = new ArrayList<>();
		try {
			if(type.equalsIgnoreCase("node")) {
				Object[] params = {projectName.trim(),projectName.trim()};
				filterDropdownList = presentationJdbcTemplate.queryForList(PyvisLibraryQueryConstant.GET_FILTER_DROPDOWN_FOR_NODE, String.class, params);
			} else if(type.equalsIgnoreCase("tech")){
				Object[] param = {projectName.trim()};
				filterDropdownList = presentationJdbcTemplate.queryForList(PyvisLibraryQueryConstant.GET_FILTER_DROPDOWN_FOR_TECH, String.class, param);
			} else {
				Object[] params = {projectName.trim(),projectName.trim()};
				filterDropdownList = presentationJdbcTemplate.queryForList(PyvisLibraryQueryConstant.GET_FILTER_DROPDOWN_FOR_SCRIPT, String.class, params);
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getFilterDropdownList DAO "+ex.getMessage());
		}
		return filterDropdownList;
	}
	
	public List<String> getTableNames(String projectName, String scriptType) {
		List<String> tableNamesList = new ArrayList<>();
		try {
			String query = "select distinct script_name from edges where trim(project_name) ='"+projectName.trim()+"' and script_type in ("+scriptType+")";
			tableNamesList = presentationJdbcTemplate.queryForList(query, String.class);
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getTableNames DAO "+ex.getMessage());
		}
		return tableNamesList;
	}
}
