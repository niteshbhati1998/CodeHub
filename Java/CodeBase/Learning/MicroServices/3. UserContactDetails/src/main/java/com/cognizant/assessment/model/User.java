package com.cognizant.assessment.model;

public class User {

	private int userId;
	private String contactInfo;
	public User(int userId, String contactInfo) {
		super();
		this.userId = userId;
		this.contactInfo = contactInfo;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getContactInfo() {
		return contactInfo;
	}
	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}
	@Override
	public String toString() {
		return "User[userId=" + userId + ", contactInfo=" + contactInfo + "]";
	}
}
