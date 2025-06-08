package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.BusinessObjectNameDetails;

public class BusinessObjectNameDetailsMapper implements RowMapper<BusinessObjectNameDetails> {
    @Override
    public BusinessObjectNameDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessObjectNameDetails details = new BusinessObjectNameDetails();
        details.setProjectName(rs.getString(1));
        details.setSourceModule(rs.getString(2));
        details.setTargetModule(rs.getString(3));
        details.setTableName(rs.getString(4));
        details.setViewName(rs.getString(5));
        details.setMaterializedViewName(rs.getString(6));
        details.setUserDefinedFunctionName(rs.getString(7));
        details.setProcedureName(rs.getString(8));
        details.setTriggerName(rs.getString(9));
        details.setJobName(rs.getString(10));
        return details;
    }
}
