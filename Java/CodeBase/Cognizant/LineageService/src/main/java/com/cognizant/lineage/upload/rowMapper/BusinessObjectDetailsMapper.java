package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.BusinessObjectDetails;

public class BusinessObjectDetailsMapper implements RowMapper<BusinessObjectDetails> {
    @Override
    public BusinessObjectDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessObjectDetails bod = new BusinessObjectDetails();
        bod.setNodeName(rs.getString(1));
        bod.setNodeType(rs.getString(2));
        bod.setModuleName(rs.getString(3));
        return bod;
    }
}
