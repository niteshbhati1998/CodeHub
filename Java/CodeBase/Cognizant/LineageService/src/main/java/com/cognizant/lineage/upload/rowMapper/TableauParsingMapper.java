package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.TableauParsing;

public class TableauParsingMapper implements RowMapper<TableauParsing>{

	@Override
	public TableauParsing mapRow(ResultSet rs, int rowNum) throws SQLException {
		TableauParsing tableauParsing = new TableauParsing();
		tableauParsing.setReportName(rs.getString("report_name"));
		tableauParsing.setColumnName(rs.getString("column_name"));
		tableauParsing.setNotRealName(rs.getString("not_real_table_name"));
		tableauParsing.setRealName(rs.getString("real_table_name"));
		tableauParsing.setJobName(rs.getString("filename"));
		tableauParsing.setJobType(rs.getString("tech"));
		tableauParsing.setJobId(rs.getInt("job_id"));
		tableauParsing.setProjectName(rs.getString("project_name"));
		return tableauParsing;
	}
}