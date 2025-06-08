package com.cognizant.lineage.security;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class ApplicationUserRowMapper implements RowMapper<ApplicationUser> {
private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUserRowMapper.class);
	
	public ApplicationUser mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
		LOGGER.debug("ApplicationUserRowMapper Mapper Row number : "+rowNumber);
		
		ApplicationUser user = new ApplicationUser();
		user.setUserId(rs.getString("USER_ID"));
		user.setPassword(rs.getString("PASSWORD"));
		user.setFirstName(rs.getString("FIRST_NAME"));
		user.setLastName(rs.getString("LAST_NAME"));
		user.setEmail(rs.getString("EMAIL"));
		user.setStatusCode(rs.getString("STATUS_CODE"));
		user.setLicenseValidator(rs.getString("LICENSE_VALIDATOR"));
		user.setCreationDate(CommonUtil.convertTimestampToLocalDateTime(rs.getTimestamp("CREATION_DATE")));
		user.setCreatedBy(rs.getString("CREATED_BY"));
		user.setLastUpdateDate(CommonUtil.convertTimestampToLocalDateTime(rs.getTimestamp("LAST_UPDATE_DATE")));
		user.setLastUpdateBy(rs.getString("LAST_UPDATE_BY"));
		user.setPasswordExpiryDate(CommonUtil.convertTimestampToLocalDateTime(rs.getTimestamp("PASSWORD_EXPIRY_DATE")));
		user.setLastLoginDate(CommonUtil.convertTimestampToLocalDateTime(rs.getTimestamp("ACCESS_DATE")));
		user.setAuthorities(rs.getString("AUTHORITIES"));
		return user;
	}
}

