package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.ColumnLineageQueryConstant;

@Repository
public class ColumnLineageDao {

	@Autowired
	@Qualifier("lineageJdbcTemplate")
	JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(ColumnLineageDao.class);

	public Long getSequenceId() {
		Long runId = 0L;
		try {

			runId = jdbcTemplate.queryForObject(ColumnLineageQueryConstant.SQL_SEQUENCE_ID, Long.class);

		} catch (Exception e) {
			LOGGER.error("Exception occured in getSequenceId",  e.getMessage());
		}

		return runId;
	}

	public List<String> getAllTableNames(String projectName) {

		List<String> names = null;
		try {
			Object param[] = { projectName };

			names = jdbcTemplate.queryForList(ColumnLineageQueryConstant.GET_ALL_TABLE_NAMES, String.class, param);

		} catch (Exception e) {
			LOGGER.error("Exception occured in getAllTableNames ", e);
		}
		if (names == null) {
			names = new ArrayList<>();
		}
		return names;
	}

	public List<String> getColumnNamesByTableName(String projectName, String tableName) {

		List<String> names = null;
		try {
			Object param[] = { projectName, tableName };

			names = jdbcTemplate.queryForList(ColumnLineageQueryConstant.GET_COLUMN_NAMES_BY_TABLE_NAME, String.class,
					param);

		} catch (Exception e) {
			LOGGER.error("Exception occured in getColumnNamesByTableName ", e);
		}
		if (names == null) {
			names = new ArrayList<>();
		}
		return names;
	}

}
