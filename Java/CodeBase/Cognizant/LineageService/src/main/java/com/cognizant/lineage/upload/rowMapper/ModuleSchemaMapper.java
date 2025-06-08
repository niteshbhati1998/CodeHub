package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.ModuleSchema;

public class ModuleSchemaMapper implements RowMapper<ModuleSchema>   {
	@Override
	public ModuleSchema mapRow(ResultSet rs, int rowNum) throws SQLException {
		ModuleSchema moduleSchema = new ModuleSchema();
		moduleSchema.setModule(rs.getString("module"));
		moduleSchema.setSchemas(rs.getString("schemas"));
		return moduleSchema;
	}
}
