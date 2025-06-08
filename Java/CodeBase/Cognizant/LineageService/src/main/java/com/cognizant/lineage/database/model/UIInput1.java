package com.cognizant.lineage.database.model;

public class UIInput1 {
	private String projectName;
	private String databaseType;
	private String connectionName;
	private boolean procedure;
	private boolean view;
	private boolean macro;
	private boolean function;
	private boolean packageName;
	private boolean trigger;
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getDatabaseType() {
		return databaseType;
	}
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	public String getConnectionName() {
		return connectionName;
	}
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
	public boolean isProcedure() {
		return procedure;
	}
	public void setProcedure(boolean procedure) {
		this.procedure = procedure;
	}
	public boolean isView() {
		return view;
	}
	public void setView(boolean view) {
		this.view = view;
	}
	public boolean isMacro() {
		return macro;
	}
	public void setMacro(boolean macro) {
		this.macro = macro;
	}
	public boolean isFunction() {
		return function;
	}
	public void setFunction(boolean function) {
		this.function = function;
	}
	public boolean isPackageName() {
		return packageName;
	}
	public void setPackageName(boolean packageName) {
		this.packageName = packageName;
	}
	public boolean isTrigger() {
		return trigger;
	}
	public void setTrigger(boolean trigger) {
		this.trigger = trigger;
	}
	
	
}
