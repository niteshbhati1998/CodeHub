package com.cognizant.lineage.upload.constants;

public class ModuleLineageQueryConstant {
	private ModuleLineageQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}
	
	public static final String GET_NODE_INFO = "select distinct node_name, node_type from nodes where project_name=?";
	
	public static final String GET_MODULE_SCHEMA_LIST = "select distinct module,schemas from semantic.module_schema_mapper where project_name = ? " + 
			"union " + 
			"select distinct module_name as module,'' as schemas from semantic.project_modules where project_name = ? " + 
			"and module_name not in (select distinct module from semantic.module_schema_mapper where project_name = ?) ";
	
	public static final String SAVE_MODULE_INFO = "insert into semantic.project_modules(project_name,module_name) values(?,?)";
	
	public static final String INSERT_INTO_MODULE_SCHEMA_MAPPER = "insert into semantic.module_schema_mapper(project_name,module,schemas) " + 
			"values(?,?,?)";

	public static final String UPDATE_MODULE_SCHEMA_MAPPER = "update semantic.module_schema_mapper set schemas=? where project_name=? and module=?" ;
	
	public static final String DELETE_FROM_MODULE_SCHEMA_MAPPER_ACC_TO_PROJECT_MODULES = "delete from semantic.module_schema_mapper where project_name=? and module not in( " + 
			"select module_name from semantic.project_modules where project_name=?)";
	
	public static final String DELETE_EXISTING_PROJECT_DETAILS_FROM_MODULE_LINEAGE_STATUS = "delete from semantic.module_lineage_status " + 
			"where project_name = ?";
	
	public static final String INSERT_INTO_MODULE_LINEAGE_STATUS = "insert into semantic.module_lineage_status(project_name,stepno,stepname,status) " + 
			"values(?,?,?,?)";
	
	public static final String DELETE_MODULE_INFO = "delete from semantic.project_modules where project_name=? and module_name=?";
	
	public static final String GET_MODULE_LINEAGE_STATUS = "select stepname, status from semantic.module_lineage_status " + 
			"where project_name = ? " + 
			"order by stepno desc limit 1";
}