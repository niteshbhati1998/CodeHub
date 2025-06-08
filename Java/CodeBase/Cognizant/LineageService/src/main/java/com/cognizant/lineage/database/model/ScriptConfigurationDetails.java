package com.cognizant.lineage.database.model;

public class ScriptConfigurationDetails {

	private String projectName;
	private String scriptName;
	private String serverAlias;
	private String inputDirectory;
	private String fileExtension;
	private String outputDirectory;
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getScriptName() {
		return scriptName;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	public String getServerAlias() {
		return serverAlias;
	}
	public void setServerAlias(String serverAlias) {
		this.serverAlias = serverAlias;
	}
	public String getInputDirectory() {
		return inputDirectory;
	}
	public void setInputDirectory(String inputDirectory) {
		this.inputDirectory = inputDirectory;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	public String getOutputDirectory() {
		return outputDirectory;
	}
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
}
