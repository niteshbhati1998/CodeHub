package com.cognizant.lineage.security;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class ReadOnlyUserRowMapper implements RowMapper<ApplicationUser> {
private static final Logger LOGGER = LoggerFactory.getLogger(ReadOnlyUserRowMapper.class);
	
	public ApplicationUser mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
		LOGGER.debug("ReadOnlyUserRowMapper Mapper Row number : "+rowNumber);
		
		ApplicationUser user = new ApplicationUser();
		user.setUserId(rs.getString("USER_ID"));
		user.setFirstName(rs.getString("FIRST_NAME"));
		user.setLastName(rs.getString("LAST_NAME"));
		return user;
	}
}

