package com.cognizant.lineage.upload.model;

import java.util.List;

public class ModuleLineageUi {

	private List<String> schemaList;
	private List<ModuleInfo> moduleList;
	public List<String> getSchemaList() {
		return schemaList;
	}
	public void setSchemaList(List<String> schemaList) {
		this.schemaList = schemaList;
	}
	public List<ModuleInfo> getModuleList() {
		return moduleList;
	}
	public void setModuleList(List<ModuleInfo> moduleList) {
		this.moduleList = moduleList;
	}
}
