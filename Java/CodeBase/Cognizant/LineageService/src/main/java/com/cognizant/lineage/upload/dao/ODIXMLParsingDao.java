package com.cognizant.lineage.upload.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.QueryConstant;

@Repository
public class ODIXMLParsingDao {

	@Autowired
	@Qualifier("lineageJdbcTemplate")
	private JdbcTemplate lineageJdbcTemplate;
	
	@Autowired
	@Qualifier("presentationJdbcTemplate")
	private JdbcTemplate presentationJdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(ODIXMLParsingDao.class);
	  
	//below method is used to get job status id using "semantic.lineage_job_status_seq"
	public Long getNextSequenceIdBteq() {
		Long sequenceId =0L;
		try {
			 sequenceId = lineageJdbcTemplate.queryForObject(QueryConstant.BTEQ_LENEAGE_EXECUTION_NEXT_SEQUENCE_ID, Long.class);
		} catch (Exception ex) {
			LOGGER.error("Exception occured in getSequenceIdBteq " + ex.getMessage());
		}
		return sequenceId;
	}
	
	public void insertIntoOdiDetails(List<Object[]> params) {
		try {
			lineageJdbcTemplate.batchUpdate(QueryConstant.INSERT_INTO_ODI_DETAILS, params);
		} catch(Exception ex) {
			LOGGER.info("Exception occured in insertIntoOdiDetails Dao "+ex.getMessage());
		}
	}
}