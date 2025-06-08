package com.cognizant.lineage.upload.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.dao.ColumnLineageDao;

@Service
public class ColumnLineageServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(ColumnLineageServiceImpl.class);

	@Autowired
	ColumnLineageDao columnLineageDao;

	public List<String> getAllTableNames(String projectName) {
		List<String> names = null;
		try {
			names = columnLineageDao.getAllTableNames(projectName);
		} catch (Exception e) {
			LOGGER.error("Exception occured in getAllTableNames ", e);
		}
		return names;
	}

	public List<String> getColumnNamesByTableName(String projectName, String tableName) {
		List<String> names = null;
		try {
			names = columnLineageDao.getColumnNamesByTableName(projectName, tableName);
		} catch (Exception e) {
			LOGGER.error("Exception occured in getColumnNamesByTableName ", e);
		}
		return names;
	}

}
