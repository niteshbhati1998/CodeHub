package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.HotSpotDetail;


public class HotSpotDetailMapper implements RowMapper<HotSpotDetail> {

    @Override
    public HotSpotDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        HotSpotDetail detail = new HotSpotDetail();
        detail.setProjectName(rs.getString(1));
        detail.setNodeName(rs.getString(2));
        detail.setIncomingEdges(rs.getInt(3));
        detail.setOutgoingEdges(rs.getInt(4));
        detail.setDegree(rs.getInt(5));
        return detail;
    }
}
