package com.cognizant.lineage.database.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.database.model.DbScriptDetails;

public class DbScriptDetailsMapper implements RowMapper<DbScriptDetails>{

	@Override
	public DbScriptDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		DbScriptDetails dbScriptDetails = new DbScriptDetails();
		dbScriptDetails.setJobId(rs.getInt("job_id"));
		dbScriptDetails.setProjectName(rs.getString("project_name"));
		dbScriptDetails.setDatabaseType(rs.getString("parent_technology"));
		dbScriptDetails.setDatabaseObjectType(rs.getString("technology"));
		dbScriptDetails.setLocation(rs.getString("uploaded_dir"));
		return dbScriptDetails;
	}
}