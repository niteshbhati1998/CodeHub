package com.cognizant.lineage.upload.model;

import java.util.List;

public class WaveDataResponse {
	
	private List<WaveTableDetails> scriptDetails;
	
	public List<WaveTableDetails> getScriptDetails() {
		return scriptDetails;
	}
	public void setScriptDetails(List<WaveTableDetails> scriptDetails) {
		this.scriptDetails = scriptDetails;
	}
}
