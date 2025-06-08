package com.cognizant.assessment.vault.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
//@AllArgsConstructor
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
