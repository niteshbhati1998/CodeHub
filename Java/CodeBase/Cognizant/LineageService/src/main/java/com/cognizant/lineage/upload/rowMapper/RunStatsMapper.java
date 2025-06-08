package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.RunStatus;

public class RunStatsMapper implements RowMapper<RunStatus> {

	@Override
	public RunStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		RunStatus run = new RunStatus();
		
		run.setExecutionId(rs.getString("execution_id"));
		run.setBatchSize(rs.getString("batch_size"));
		run.setSuccessCount(rs.getString("success_count"));
		run.setFailCount(rs.getString("fail_count"));
		
		return run;
	}

}
