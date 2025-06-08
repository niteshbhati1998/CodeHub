package com.cognizant.lineage.upload.constants;

public class HotspotLineageQueryConstant {
	
		private HotspotLineageQueryConstant() {
			throw new IllegalAccessError("Utility class");
		}
		
		private static final String NODE_DB = "nodes";
		private static final String EDGE_TABLE = "edges";
		//private static final String GRAPH_ISLAND_JSON ="presentation.graph_island_wise_json_dump";
		
		public static final String GET_NODE_NAMES = "select distinct node_name from " + NODE_DB + " where project_name = ?";
		
		public static final String GET_NODE_NAMES_APPLICATION_BASED = "select distinct node_name from presentation.script_nodes where project_name = ?";
		
		private static final String GRAPH_ISLAND_JSON_TEST = "presentation.graph_island_wise_json_dump_test";
		
		public static final String GET_NODE_DATA = "select ROW_NUMBER() OVER(ORDER BY (SELECT 1)) AS serial_no, node_name, project_name, domain_name, layer_name, " + 
				  "incoming_edges, outgoing_edges, degree from " + NODE_DB + " where project_name=?";	//"pagerank,
		
		public static final String GET_DATABSE_TABLE_MAPPPING = "select node_name from " + NODE_DB + " where project_name=? "
				+ " and island_id not in (select DISTINCT island_id  from "+ EDGE_TABLE + " where project_name= ? and script_type='SHELL')";
		
		public static final String GET_GRAPH_ISLAND_JSON = 
				"select json_dump from " + GRAPH_ISLAND_JSON_TEST + " where upper(json_dump) like ? and project_name=? " 
				+ " and (json_type != 'shell' or json_type is null) limit 1";
		
		public static final String GET_PROJECT_NAMES = "select distinct project_name from project_names order by project_name";
				 
	
	
}
