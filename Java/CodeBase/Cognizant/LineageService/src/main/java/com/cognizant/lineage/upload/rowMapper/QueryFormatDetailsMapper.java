package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.QueryFormatDetails;

public class QueryFormatDetailsMapper implements RowMapper<QueryFormatDetails> {

	@Override
	public QueryFormatDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		QueryFormatDetails queryFormatDetails = new QueryFormatDetails();
		queryFormatDetails.setFunctionName(rs.getString("function_name").toUpperCase());
		queryFormatDetails.setStartArgument(rs.getInt("start_argument"));
		queryFormatDetails.setEndArgument(rs.getInt("end_argument"));
		return queryFormatDetails;
	}	
}
