package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostgresDetails {
    @JsonProperty(value= "key", required = true)
    public String key;
    @JsonProperty(value= "value", required = true)
    public String value;
    
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public PostgresDetails(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	public PostgresDetails() {
		super();
	}	
}