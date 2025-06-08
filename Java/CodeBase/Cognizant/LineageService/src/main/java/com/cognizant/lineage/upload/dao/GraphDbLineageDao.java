package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.HotspotLineageQueryConstant;
import com.cognizant.lineage.upload.model.DatabaseWithTableMapping;
import com.cognizant.lineage.upload.rowMapper.DatabseWithTableMappingMapper;

@Repository
public class GraphDbLineageDao {
	
	@Autowired
	@Qualifier("presentationJdbcTemplate")
	private JdbcTemplate presentationJdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphDbLineageDao.class);

	public List<DatabaseWithTableMapping> getDatabaseWithTable(String projectName) {
		
		List<DatabaseWithTableMapping> databaseWithTableMapping = new ArrayList<>();
		try {
		
			Object[] param = {projectName, projectName};
			databaseWithTableMapping = 
					presentationJdbcTemplate.query(HotspotLineageQueryConstant.GET_DATABSE_TABLE_MAPPPING, 
							new DatabseWithTableMappingMapper(), param);
		
		} catch (Exception e) {
			LOGGER.error("Exception occured in dao -> getDatabaseWithTable. " +e.getMessage());
		}
		return databaseWithTableMapping;
	}	
}