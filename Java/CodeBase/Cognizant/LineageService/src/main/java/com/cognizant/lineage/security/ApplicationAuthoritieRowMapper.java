package com.cognizant.lineage.security;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class ApplicationAuthoritieRowMapper implements RowMapper<ApplicationAuthoritie> {
private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAuthoritieRowMapper.class);
	
	public ApplicationAuthoritie mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
		LOGGER.debug("ApplicationAuthoritie Mapper Row number : "+rowNumber);
		
		ApplicationAuthoritie authoritie = new ApplicationAuthoritie();
		authoritie.setUserId(rs.getString("USER_ID"));
		authoritie.setAuthority(rs.getString("AUTHORITY"));
		
		return authoritie;
	}
}

