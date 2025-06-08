package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.ScriptCalculationDetails;

public class ScriptCalculationDetailsMapper implements RowMapper<ScriptCalculationDetails>{

	@Override
	public ScriptCalculationDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		ScriptCalculationDetails scriptCalculationDetails = new ScriptCalculationDetails();
		scriptCalculationDetails.setFunctionCount(rs.getInt("function_used_count"));
		scriptCalculationDetails.setJoinCount(rs.getInt("join_count"));
		scriptCalculationDetails.setSelectCount(rs.getInt("select_count"));
		scriptCalculationDetails.setTransformationCount(rs.getInt("other_component_count"));
		return scriptCalculationDetails;
	}
}