package com.cognizant.lineage.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VaultDataWithAllFields {
    @JsonProperty(value= "key", required = true)
    public String key;
    @JsonProperty(value= "value", required = true)
    public DBObject value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public DBObject getValue() {
		return value;
	}
	public void setValue(DBObject value) {
		this.value = value;
	}
	public VaultDataWithAllFields(String key, DBObject value) {
		super();
		this.key = key;
		this.value = value;
	}
	public VaultDataWithAllFields() {
		super();
	}
	
}