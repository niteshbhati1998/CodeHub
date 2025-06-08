package com.cognizant.lineage.upload.service;

public class PiScriptCountModule {
	
	private String module;
	private Object scriptCount;
	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public Object getScriptCount() {
		return scriptCount;
	}
	public void setScriptCount(Object scriptCount) {
		this.scriptCount = scriptCount;
	}
	@Override
	public String toString() {
		return "PiScriptCountModule [module=" + module + ", scriptCount=" + scriptCount + "]";
	}

}
