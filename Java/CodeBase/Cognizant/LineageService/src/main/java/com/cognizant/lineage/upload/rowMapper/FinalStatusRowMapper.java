package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.FinalStatus;

public class FinalStatusRowMapper implements RowMapper<FinalStatus>{

	@Override
	public FinalStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		FinalStatus finalStatus = new FinalStatus();
		finalStatus.setRecordsCount(rs.getInt("total_record_processed"));
		finalStatus.setLogLocation(rs.getString("log_file"));
		
		return finalStatus;
	}

}
