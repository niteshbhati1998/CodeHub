package com.cognizant.lineage.upload.model;

import java.util.List;
import java.util.Map;

public class ModuleLineage {

	private Map<String,List<String>> map;

	public Map<String, List<String>> getMap() {
		return map;
	}

	public void setMap(Map<String, List<String>> map) {
		this.map = map;
	}
}
