package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.ClusterReport;

public class ClusterReportMapper implements RowMapper<ClusterReport> {
    @Override
    public ClusterReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClusterReport report = new ClusterReport();
        report.setIslandId(rs.getInt("island_id"));
        report.setSourceObject(rs.getString("from_node"));
        report.setTargetObject(rs.getString("to_node"));
        report.setScriptName(rs.getString("script_name"));
        report.setScriptType(rs.getString("script_type"));
        report.setStatementType(rs.getString("statement_type"));
        return report;
    }
}
