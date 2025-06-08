package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.InformaticaParamTables;

public class InformaticaParamTablesMapper implements RowMapper<InformaticaParamTables>{

	@Override
	public InformaticaParamTables mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		InformaticaParamTables informaticaParamTables = new InformaticaParamTables();
		informaticaParamTables.setFileName(rs.getString("file_name"));
		informaticaParamTables.setSessionName(rs.getString("session_name"));
		informaticaParamTables.setFromTable(rs.getString("from_table"));
		informaticaParamTables.setToTable(rs.getString("to_table"));	
		informaticaParamTables.setParamFilename(rs.getString("param_filename"));
		return informaticaParamTables;
	}
}