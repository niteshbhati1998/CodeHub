package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.ModuleLineageStatus;

public class ModuleLineageStatusMapper implements RowMapper<ModuleLineageStatus>   {
	@Override
	public ModuleLineageStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
		ModuleLineageStatus moduleLineageStatus = new ModuleLineageStatus();
		moduleLineageStatus.setStepName(rs.getString("stepname"));
		moduleLineageStatus.setStatus(rs.getString("status"));
		return moduleLineageStatus;
	}
}
