package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.BusinessObjectCountDetails;

public class BusinessObjectCountDetailsMapper implements RowMapper<BusinessObjectCountDetails> {
    @Override
    public BusinessObjectCountDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessObjectCountDetails details = new BusinessObjectCountDetails();
        details.setProjectName(rs.getString(1));
        details.setSourceModule(rs.getString(2));
        details.setTargetModule(rs.getString(3));
        details.setTableCount(rs.getInt(4));
        details.setViewCount(rs.getInt(5));
        details.setMaterializedViewCount(rs.getInt(6));
        details.setUserDefinedFunctionCount(rs.getInt(7));
        details.setProcedureCount(rs.getInt(8));
        details.setTriggerCount(rs.getInt(9));
        details.setJobCount(rs.getInt(10));
        return details;
    }
}
