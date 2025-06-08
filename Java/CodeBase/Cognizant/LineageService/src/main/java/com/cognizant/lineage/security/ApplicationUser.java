package com.cognizant.lineage.security;


import java.time.LocalDateTime;

public class ApplicationUser {

	private String userId;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private LocalDateTime creationDate;
	private String creationDateFmt;
	private String statusCode;
	private String licenseValidator;
	private String createdBy;
	private String lastUpdateBy;
	private LocalDateTime lastUpdateDate;
	private String lastUpdateDateFmt;
	private LocalDateTime passwordExpiryDate;
	private String passwordExpiryDateFmt;
	private LocalDateTime lastLoginDate;
	private String lastLoginDateFmt;
	private String authorities;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getLicenseValidator() {
		return licenseValidator;
	}
	public void setLicenseValidator(String licenseValidator) {
		this.licenseValidator = licenseValidator;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getLastUpdateBy() {
		return lastUpdateBy;
	}
	public void setLastUpdateBy(String lastUpdateBy) {
		this.lastUpdateBy = lastUpdateBy;
	}
	public LocalDateTime getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public LocalDateTime getPasswordExpiryDate() {
		return passwordExpiryDate;
	}
	public void setPasswordExpiryDate(LocalDateTime passwordExpiryDate) {
		this.passwordExpiryDate = passwordExpiryDate;
	}
	public String getCreationDateFmt() {
		this.creationDateFmt = CommonUtil.formatLocalDateTimeToString(creationDate);
		return creationDateFmt;
	}
	public String getLastUpdateDateFmt() {
		this.lastUpdateDateFmt = CommonUtil.formatLocalDateTimeToString(lastUpdateDate);
		return lastUpdateDateFmt;
	}
	public String getPasswordExpiryDateFmt() {
		this.passwordExpiryDateFmt = CommonUtil.formatLocalDateTimeToString(passwordExpiryDate);
		return passwordExpiryDateFmt;
	}
	public LocalDateTime getLastLoginDate() {
		return lastLoginDate;
	}
	public void setLastLoginDate(LocalDateTime lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	public String getLastLoginDateFmt() {
		this.lastLoginDateFmt = CommonUtil.formatLocalDateTimeToString(lastLoginDate);
		return lastLoginDateFmt;
	}
	public String getAuthorities() {
		return authorities;
	}
	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}
}
