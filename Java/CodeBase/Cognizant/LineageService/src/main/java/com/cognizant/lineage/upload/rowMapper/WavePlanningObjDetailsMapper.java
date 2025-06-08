package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.WavePlanningObjDetails;

public class WavePlanningObjDetailsMapper implements RowMapper<WavePlanningObjDetails> {
    @Override
    public WavePlanningObjDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        WavePlanningObjDetails details = new WavePlanningObjDetails();
        details.setProjectName(rs.getString(1));
        details.setFunctionalModule(rs.getString(2));
        details.setDatabase(rs.getString(3));
        details.setObjectName(rs.getString(4));
        details.setObjectType(rs.getString(5));
        details.setComplexity(rs.getString(6));
        details.setObjectSize(rs.getString(7));
        details.setWave(rs.getInt(8));
        return details;
    }
}
