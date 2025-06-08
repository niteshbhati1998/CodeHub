package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.SpaceAndComplexity;

public class SpaceAndComplexityMapper implements RowMapper<SpaceAndComplexity> {
    @Override
    public SpaceAndComplexity mapRow(ResultSet rs, int rowNum) throws SQLException {
        SpaceAndComplexity complexity = new SpaceAndComplexity();
        complexity.setSpace(rs.getString(1));
        complexity.setComplexity(rs.getString(2));
        return complexity;
    }
}
