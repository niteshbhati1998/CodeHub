package com.cognizant.lineage.upload.model;

import java.util.List;

public class ModuleInfo {

	private String title;
	private String id;
	private List<ModuleInfo> childNodes;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<ModuleInfo> getChildNodes() {
		return childNodes;
	}
	public void setChildNodes(List<ModuleInfo> childNodes) {
		this.childNodes = childNodes;
	}
}
