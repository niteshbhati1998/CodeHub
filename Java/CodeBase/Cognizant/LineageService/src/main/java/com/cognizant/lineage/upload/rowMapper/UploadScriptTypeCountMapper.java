package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.UploadScriptTypeCount;

public class UploadScriptTypeCountMapper implements RowMapper<UploadScriptTypeCount> {

	@Override
	public UploadScriptTypeCount mapRow(ResultSet rs, int rowNum) throws SQLException {
		UploadScriptTypeCount uploadScriptTypeCount = new UploadScriptTypeCount();		
		uploadScriptTypeCount.setScriptType(getScriptType(rs.getString("script_type")));
		uploadScriptTypeCount.setCount(rs.getInt("count"));
		return uploadScriptTypeCount;
	}
	
	public String getScriptType(String scriptType) {
		scriptType = scriptType.toLowerCase();
		
		if(scriptType.contains("-")) {
			if(scriptType.contains("procedure") || (!scriptType.contains("qlikview") && scriptType.contains("view")) || scriptType.contains("function") || scriptType.contains("trigger") || scriptType.contains("power-bi")) {
				String firstVal =  scriptType.split("-")[0];
	    		String secondVal = scriptType.split("-")[1];
	    		return firstVal.substring(0, 1).toUpperCase() + firstVal.substring(1) + "-" + secondVal.substring(0, 1).toUpperCase() + secondVal.substring(1);
			}
			else {
				String secondVal = scriptType.split("-")[1];
				return secondVal.substring(0, 1).toUpperCase() + secondVal.substring(1);
			}
		} else {
    		return scriptType.substring(0, 1).toUpperCase() + scriptType.substring(1);
		}
	}
}