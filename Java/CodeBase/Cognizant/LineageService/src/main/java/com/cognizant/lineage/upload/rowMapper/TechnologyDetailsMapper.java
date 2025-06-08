package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.TechnologyDetails;

public class TechnologyDetailsMapper implements RowMapper<TechnologyDetails> {

	@Override
	public TechnologyDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		TechnologyDetails technologyDetails = new TechnologyDetails();
		technologyDetails.setScriptName(rs.getString("script_name"));
		technologyDetails.setSource(rs.getString("source"));
		technologyDetails.setTarget(rs.getString("target"));
		technologyDetails.setSqlText(rs.getString("sql_text"));
		technologyDetails.setStatementType(rs.getString("statement_type"));
		return technologyDetails;
	}
}