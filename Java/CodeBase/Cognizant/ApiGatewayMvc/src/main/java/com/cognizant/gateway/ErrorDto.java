package com.cognizant.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorDto {
	
	@JsonProperty(value = "message")
	private String message;
	
	@JsonProperty(value = "details")
	private String details;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
}
