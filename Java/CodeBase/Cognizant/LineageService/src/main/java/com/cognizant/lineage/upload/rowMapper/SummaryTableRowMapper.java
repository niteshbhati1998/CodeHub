package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.SummaryTable;

public class SummaryTableRowMapper implements RowMapper<SummaryTable> {

	@Override
	public SummaryTable mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		SummaryTable summary = new SummaryTable();
		summary.setExecutionId(rs.getString("execution_id"));
		summary.setBatchSize(rs.getString("batch_size"));
		summary.setFileName(rs.getString("file_name"));
		summary.setLogFile(rs.getString("log_file"));
		summary.setStatus(rs.getString("status"));
		
		//.split("\\+")[0]+" UTC"
		summary.setStartTime(rs.getString("start_time"));
		summary.setEndTime(rs.getString("end_time"));
		
		return summary;
	}

}
