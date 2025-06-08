package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.NodeInfo;

public class NodeInfoMapper implements RowMapper<NodeInfo> {

	@Override
	public NodeInfo mapRow(ResultSet rs, int rowNum) throws SQLException {	
		NodeInfo nodeInfo = new NodeInfo();
		nodeInfo.setNodeName(rs.getString("node_name"));
		nodeInfo.setNodeType(rs.getString("node_type"));
		return nodeInfo;
	}
}
