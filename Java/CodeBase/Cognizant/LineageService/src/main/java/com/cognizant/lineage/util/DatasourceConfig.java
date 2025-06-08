package com.cognizant.lineage.util;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import com.cognizant.lineage.upload.model.PostgresDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DatasourceConfig {

	@Value("${vault.url}")
	private String vaultURL;
	
	@Value("${vault.path}")
	private String vaultPath;
	
	@Value("${lineage.schema}")
	private String lineageSchema;
	
	@Value("${lineage.presentation.schema}")
	private String lineagePresentationSchema;
	
	private static final Logger LOGGER = LoggerFactory.getLogger("DatasourceConfig");
	
	DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

	@Bean(name = "lineageDataSource")
	@Primary
	public DataSource getLineageDataSource() {
		LOGGER.info("DataSource creation started");
		try {
			String url = getPostgresDetailsFromVault() + "?currentSchema=" + lineageSchema;
			dataSourceBuilder.url(url);
			LOGGER.info("DataSource creation successfully");
		}catch(Exception ex) {
			LOGGER.info("Exception occurred in dataSource creation " + ex.getMessage());
		}
		return dataSourceBuilder.build();
	}

	@Bean(name = "lineageJdbcTemplate")
	public JdbcTemplate jdbcTemplate1(@Qualifier("lineageDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}
	
	@Bean(name = "presentationDataSource")
	public DataSource getPresentationDataSource() {
		LOGGER.info("DataSource creation started");
		try {
			String url = getPostgresDetailsFromVault() + "?currentSchema=" + lineagePresentationSchema;
			dataSourceBuilder.url(url);
			LOGGER.info("DataSource creation successfully");
		}catch(Exception ex) {
			LOGGER.info("Exception occurred in dataSource creation " + ex.getMessage());
		}
		return dataSourceBuilder.build();
	}
	
	@Bean(name = "presentationJdbcTemplate")
	public JdbcTemplate jdbcTemplate2(@Qualifier("presentationDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

	
	public String getPostgresDetailsFromVault() {
	    String host = "",port = "",dbName = "",username = "",password = "",driver = "",connectionUrl="";
		
	    //fetching details from vault
		List<PostgresDetails> postgresDetailsList = new ArrayList<>();
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Content-Type", "application/json");

	    String requestBody = "{\"path\": \""+ vaultPath +"\"}";
	    HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody,headers);
	    RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(vaultURL, HttpMethod.POST, requestEntity, String.class);
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        postgresDetailsList = objectMapper.readValue(
	                response.getBody(),
	                new TypeReference<>() {}
	        );
	    } catch(Exception ex) {
			LOGGER.info("Exception occurred in getPostgresDetailsFromVault "+ex.getMessage());
		}
	    
	    for(PostgresDetails postgresDetails: postgresDetailsList) {
			if(postgresDetails.getKey().equals("hostname")) {
				host = postgresDetails.getValue();                        
			} else if(postgresDetails.getKey().equals("port")) {
				port = postgresDetails.getValue();
			} else if(postgresDetails.getKey().equals("dbname")) {
				dbName = postgresDetails.getValue();
			} else if(postgresDetails.getKey().equals("username")) {
				username = postgresDetails.getValue();
			} else if(postgresDetails.getKey().equals("password")) {
				password = postgresDetails.getValue();
			} else if(postgresDetails.getKey().equals("driver")) {
				driver = postgresDetails.getValue();
			} 
		}
	    connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
	    dataSourceBuilder.driverClassName(driver);
		dataSourceBuilder.username(username);
		dataSourceBuilder.password(password);
		return connectionUrl;
	}
}
