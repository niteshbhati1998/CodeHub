package com.cognizant.lineage.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DBObject {
    @JsonProperty(value= "dbtype", required = false)
    public String dbtype;
    @JsonProperty(value= "host",required = false)
    public String host;
    @JsonProperty(value= "port",required = false)
    public String port;
    @JsonProperty(value= "databasename",required = false)
    public String databasename;
    @JsonProperty(value= "username",required = false)
    public String username;
    @JsonProperty(value= "password",required = false)
    public String password;

    public String getDbtype() {
		return dbtype;
	}

	public void setDbtype(String dbtype) {
		this.dbtype = dbtype;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabasename() {
		return databasename;
	}

	public void setDatabasename(String databasename) {
		this.databasename = databasename;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DBObject(String dbtype, String host, String port, String databasename, String username, String password) {
		super();
		this.dbtype = dbtype;
		this.host = host;
		this.port = port;
		this.databasename = databasename;
		this.username = username;
		this.password = password;
	}
	
	public DBObject() {
		super();
	}

	public DBObject(String value) {
        try {
            DBObject temp = new ObjectMapper().readValue(value, DBObject.class);
            this.dbtype = temp.getDbtype();
            this.host=temp.getHost();
            this.port = temp.getPort();
            this.databasename = temp.getDatabasename();
            this.username = temp.getUsername();
            this.password = temp.getPassword();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}