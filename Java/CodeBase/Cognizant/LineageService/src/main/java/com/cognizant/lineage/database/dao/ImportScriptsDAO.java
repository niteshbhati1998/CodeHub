package com.cognizant.lineage.database.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.database.constants.ImportScriptsQueryConstant;

@Repository
public class ImportScriptsDAO {
	  
	@Autowired
	@Qualifier("lineageJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	public int getJobId(Logger LOGGER) {
		int jobId = 0;
		try {
			jobId = jdbcTemplate.queryForObject(ImportScriptsQueryConstant.GET_JOB_ID, Integer.class);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getJobId DAO " + ex.getMessage());
		}
		return jobId;
	}

	public void insertIntoOdiDetails(List<Object[]> params, Logger LOGGER) {
		try {
			jdbcTemplate.batchUpdate(ImportScriptsQueryConstant.INSERT_INTO_ODI_DETAILS, params);
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in insertIntoOdiDetails Dao "+ex.getMessage());
		}
	}
	
	public int getStepNo(int jobId, Logger LOGGER) {
		int stepNo = 0;
		try {
			Object[] param = {jobId};
			stepNo = jdbcTemplate.queryForObject(ImportScriptsQueryConstant.GET_STEP_NO, Integer.class, param);
		} catch (Exception ex) {
			stepNo=1;
			LOGGER.info("Exception occurred in getStepNo " + ex.getMessage());
		}
		return stepNo;
	}
	
	public void insertIntoLineageJob(String jobId, String projectName, String parentTechnology, String technology, String uploadType, String uploadDirectory, Logger LOGGER) {
		try {
			Object[] params = {Integer.parseInt(jobId),projectName,parentTechnology,technology,uploadType,uploadDirectory};
			jdbcTemplate.update(ImportScriptsQueryConstant.INSERT_INTO_LINEAGE_JOB, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in insertIntoLineageJob " + ex.getMessage());
		}
	}
	
	public void updateLineageJob(String jobId, Timestamp startTime, Timestamp endTime, Logger LOGGER) {
		try {
			Object[] params = {startTime, endTime, Integer.parseInt(jobId)};
			jdbcTemplate.update(ImportScriptsQueryConstant.UPDATE_LINEAGE_JOB, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in insertIntoLineageJob " + ex.getMessage());
		}
	}
	
	public void insertIntoLineageJobStatus(int jobId, int stepNo, String stepName, int noOfFileReceived, int noOfFileProcessed, String status, String logFileLocation, Logger LOGGER) {
		try {
			Object[] params = {jobId,stepNo,stepName,noOfFileReceived,noOfFileProcessed,status,logFileLocation};
			jdbcTemplate.update(ImportScriptsQueryConstant.INSERT_INTO_LINEAGE_JOB_STATUS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in insertIntoLineageJobStatus " + ex.getMessage());
		}
	}
	
	public void updateLineageJobStatus(int noOfFileProcessed, String status, int jobId, int stepNo, Logger LOGGER) {
		try {
			Object[] params = {noOfFileProcessed,status,jobId,stepNo};
			jdbcTemplate.update(ImportScriptsQueryConstant.UPDATE_LINEAGE_JOB_STATUS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in updateLineageJobStatus " + ex.getMessage());
		}
	}
	
	public void updateLineageJobStatusForErrorOrCompleted(String status, int jobId, int stepNo, Logger LOGGER) {
		try {
			Object[] params = {status,jobId,stepNo};
			jdbcTemplate.update(ImportScriptsQueryConstant.UPDATE_LINEAGE_JOB_STATUS_FOR_ERROR_OR_COMPLETED, params);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occurred in updateLineageJobStatusForErrorOrCompleted " + ex.getMessage());
		}
	}
}
