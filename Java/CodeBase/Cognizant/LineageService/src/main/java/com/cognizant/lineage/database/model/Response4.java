package com.cognizant.lineage.database.model;

import java.util.HashMap;

public class Response4 {

	private String message;
	private HashMap<Integer, String> payload;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public HashMap<Integer, String> getPayload() {
		return payload;
	}
	public void setPayload(HashMap<Integer, String> payload) {
		this.payload = payload;
	}
}
