package com.cognizant.lineage.upload.model;

public class ErrorResponse {

	private Integer errorCode;

    private String message;
    
    public ErrorResponse(int errorCode, String message) {
    	this.errorCode = errorCode;
    	this.message = message;
	}

	public ErrorResponse() {
		super();
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
    
}
