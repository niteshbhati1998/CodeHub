package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.NodeDataModel;

public class NodeDataRowMapper implements RowMapper<NodeDataModel> {

	@Override
	public NodeDataModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			NodeDataModel nodeData = new NodeDataModel();
			nodeData.setSerial(rs.getInt("serial_no"));
			nodeData.setTableName(rs.getString("node_name"));
			nodeData.setDomainName(rs.getString("domain_name"));
			nodeData.setLayerName(rs.getString("layer_name"));
//			nodeData.setPagerank(rs.getDouble("pagerank"));
			nodeData.setInEdges(rs.getInt("incoming_edges"));
			nodeData.setOutEdges(rs.getInt("outgoing_edges"));
			nodeData.setDegree(rs.getInt("degree"));
			nodeData.setProjectName(rs.getString("project_name"));
		
		return nodeData;
	}
	
	

}
