package com.cognizant.lineage.upload.dao;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.TableauQueryConstant;

@Repository
public class TableauDAO {

	@Autowired
	@Qualifier("lineageJdbcTemplate")
	private JdbcTemplate lineageJdbcTemplate;
	
	public void insertIntoTableauParsing(int jobId, String projectName, String reportName, String workbookName, String columnName, String notRealName, String realTableName, String filename, String tech, String status, Logger LOGGER) {
		try {
			Object[] params = {jobId,projectName,reportName,workbookName,columnName,notRealName,realTableName,filename,tech,status};
			lineageJdbcTemplate.update(TableauQueryConstant.INSERT_INTO_TABLEAU_PARSING, params);
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in insertIntoTableauParsing "+ex.getMessage());
		}
	}
	
	public Long getNextSequenceIdBteq(Logger LOGGER) {
		Long sequenceId =0L;
		try {
			 sequenceId = lineageJdbcTemplate.queryForObject(TableauQueryConstant.BTEQ_LENEAGE_EXECUTION_NEXT_SEQUENCE_ID, Long.class);	
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getNextSequenceIdBteq "+ex.getMessage());
		}
		return sequenceId;
	}
	
	public int getStepNo(int jobId, Logger LOGGER) {
		int stepNo = 0;
		try {
			Object[] param = {jobId};
			stepNo = lineageJdbcTemplate.queryForObject(TableauQueryConstant.GET_STEP_NO, Integer.class, param);
		} catch (Exception ex) {
			stepNo=1;
			LOGGER.info("Exception occurred in getStepNo " + ex.getMessage());
		}
		return stepNo;
	}
}
