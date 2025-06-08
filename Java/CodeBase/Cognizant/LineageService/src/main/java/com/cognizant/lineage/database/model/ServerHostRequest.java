package com.cognizant.lineage.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServerHostRequest {
	
	@JsonProperty("scriptType")
	private String scriptType;

	public ServerHostRequest(String scriptType) {
		this.scriptType = scriptType;
	}

	public ServerHostRequest() {
		super();
	}
}
