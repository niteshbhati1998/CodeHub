package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.BusinessLineageQueryConstant;

@Repository
public class BusinessLineageDAO {

	@Autowired
	@Qualifier("presentationJdbcTemplate")
    JdbcTemplate jdbcTemplate;

	public List<String> getProjectNamesList(Logger LOGGER) {
		List<String> projectNamesList = new ArrayList<>();
		try {
			projectNamesList = jdbcTemplate.queryForList(BusinessLineageQueryConstant.GET_PROJECT_NAMES_LIST, String.class);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getProjectNamesList DAO "+ex.getMessage());
		}
		return projectNamesList;
	}
}