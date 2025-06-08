package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.SummaryQueryConstant;
import com.cognizant.lineage.upload.model.TechnologyDetails;
import com.cognizant.lineage.upload.model.UploadScriptDetails;
import com.cognizant.lineage.upload.model.UploadScriptTypeCount;
import com.cognizant.lineage.upload.rowMapper.TechnologyDetailsMapper;
import com.cognizant.lineage.upload.rowMapper.UploadScriptDetailsMapper;
import com.cognizant.lineage.upload.rowMapper.UploadScriptTypeCountMapper;

@Repository
public class SummaryDao {

	@Autowired
	@Qualifier("lineageJdbcTemplate")
	private JdbcTemplate lineageJdbcTemplate;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryDao.class);
	
	public List<UploadScriptDetails> getScriptDetails(Object projectName) {
		List<UploadScriptDetails> uploadScriptDetailsList = new ArrayList<>();
		try {
			uploadScriptDetailsList = lineageJdbcTemplate.query(SummaryQueryConstant.GET_SCRIPT_DETAILS, new UploadScriptDetailsMapper(), projectName);
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in getScriptDetails Dao "+ex.getMessage());
		}
		return uploadScriptDetailsList;
	}
	
	public List<UploadScriptTypeCount> getScriptTypeCount(Object projectName) {
		List<UploadScriptTypeCount> uploadScriptTypeCountList = new ArrayList<>();
		try {
			uploadScriptTypeCountList = lineageJdbcTemplate.query(SummaryQueryConstant.GET_SCRIPT_TYPE_COUNT_DETAILS, new UploadScriptTypeCountMapper(), projectName);
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in getScriptTypeCount Dao "+ex.getMessage());
		}
		return uploadScriptTypeCountList;
	}
	
	public List<String> getTechnologyList(Object projectName) {
		List<String> technologyList = new ArrayList<>();
		try {
			technologyList = lineageJdbcTemplate.queryForList(SummaryQueryConstant.GET_TECHNOLOGY_LIST, String.class, projectName);
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in getTechnologyList Dao "+ex.getMessage());
		}
		return technologyList;
	}
	
	public List<TechnologyDetails> fetchTechnologyDetails(String projectName, String technology) {
		List<TechnologyDetails> technologyDetailsList = new ArrayList<>();
		try {
			Object[] params = {projectName, technology};
			technologyDetailsList = lineageJdbcTemplate.query(SummaryQueryConstant.FETCH_TECHNOLOGY_DETAILS, new TechnologyDetailsMapper(), params);
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in fetchTechnologyDetails Dao "+ex.getMessage());
		}
		return technologyDetailsList;
	}
}
