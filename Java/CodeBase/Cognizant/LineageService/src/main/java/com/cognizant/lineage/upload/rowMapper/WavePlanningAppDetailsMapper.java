package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.WavePlanningAppDetails;

public class WavePlanningAppDetailsMapper implements RowMapper<WavePlanningAppDetails> {
    @Override
    public WavePlanningAppDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        WavePlanningAppDetails details = new WavePlanningAppDetails();
        details.setProjectName(rs.getString(1));
        details.setModule(rs.getString(2));
        details.setApplicationType(rs.getString(3));
        details.setTechnology(rs.getString(4));
        details.setScriptName(rs.getString(5));
        details.setComplexity(rs.getString(6));
        details.setSprint(rs.getInt(7));
        return details;
    }
}
