package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.Datastaging;

public class DataStageRowMapper implements RowMapper<Datastaging>   {
	@Override
	public Datastaging mapRow(ResultSet rs, int rowNum) throws SQLException {
		Datastaging datastaging = new Datastaging();
		datastaging.setItem(rs.getString("item"));
		datastaging.setIdentifier(rs.getString("identifier"));
		datastaging.setInputPin(rs.getString("inputpins"));
		datastaging.setOutputPin(rs.getString("outputpins"));
		datastaging.setParnter(rs.getString("partner"));
		datastaging.setLookup(rs.getString("lookup"));
		datastaging.setJobName(rs.getString("jobname"));
		return datastaging;
	}
}
