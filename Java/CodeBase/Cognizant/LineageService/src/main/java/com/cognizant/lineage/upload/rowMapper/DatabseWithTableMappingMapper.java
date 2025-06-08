package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.DatabaseWithTableMapping;

public class DatabseWithTableMappingMapper implements RowMapper<DatabaseWithTableMapping> {

	@Override
	public DatabaseWithTableMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
		DatabaseWithTableMapping databaseWithTableMapping=new DatabaseWithTableMapping();
		databaseWithTableMapping.setDatabaseName(rs.getString("node_name"));	
		return databaseWithTableMapping;
	}
	
	


}
