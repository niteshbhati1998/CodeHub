package com.cognizant.lineage.database.model;

public class CommonResponse<T> {
	
	private String message;
	
	private T payload;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

}
