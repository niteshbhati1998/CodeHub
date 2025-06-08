package com.cognizant.lineage.security;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDetailsDAO {

	private static Logger LOGGER = LoggerFactory.getLogger(UserDetailsDAO.class);
	
	@Autowired
	Environment env;
	
	@Autowired
	@Qualifier("lineageJdbcTemplate")
	JdbcTemplate jdbcTemplate;

	public List<ApplicationUser> getUserByUseId(String userId) {
		LOGGER.info("Entry getUserByUseId::");
		List<ApplicationUser> userInfoList = null;
		Object[] params = new Object[] { userId, CommonUtil.currentUtcTimestamp() };
		try {

			userInfoList = jdbcTemplate.query(QueryConstantSecurity.USER_DTL, new ApplicationUserRowMapper(), params);
			LOGGER.info("After Query Execution ::");

		} catch (Exception e) {
			LOGGER.error("Exception in UserDetailsDAO :: getUserByUseId ", e);
		}
		LOGGER.info("Exit getUserByUseId::");
		return ListUtils.emptyIfNull(userInfoList);
	}

	public List<ApplicationAuthoritie> getAuthoritieByUseId(String userId) {
		LOGGER.info("Entry getAuthoritieByUseId::");
		List<ApplicationAuthoritie> authoritieList = null;
		Object[] params = new Object[] { userId };
		try {

			authoritieList = jdbcTemplate.query(QueryConstantSecurity.AUTHORITY_DTL, new ApplicationAuthoritieRowMapper(),
					params);
			LOGGER.info("After Query Execution ::");

		} catch (Exception e) {
			LOGGER.error("Exception in UserDetailsDAO :: getAuthoritieByUseId ", e);
		}
		LOGGER.info("Exit getUserByUseId::");
		return ListUtils.emptyIfNull(authoritieList);
	}
	
	public void addUserAndAuthoritie(ApplicationUserUIBean user, String createdBy) {
		try {		
			Object param[]= {user.getUserId(),user.getPassword(),user.getFirstName(),user.getLastName(),user.getEmail(),
					ApplicationConstant.USER_STATUS_ACTIVE,
					ApplicationConstant.LICENSE_VALIDATOR_YES,CommonUtil.currentUtcTimestamp(),
					createdBy,createdBy,CommonUtil.currentUtcTimestamp(),
					CommonUtil.addDaysToCurrentUtcTimestampSql(Long.parseLong(env.getProperty("initialPasswordValidityInDays")))};			
			jdbcTemplate.update(QueryConstantSecurity.USER_DTL_INSERT, param);
			Object paramAuth[]= {user.getUserId(),user.getRole().toUpperCase()};
			jdbcTemplate.update(QueryConstantSecurity.AUTHORITY_DTL_INSERT, paramAuth);
		} catch(Exception e) {
			LOGGER.error("Exception occured in add User And Authoritie", e);
			throw new RuntimeException(e);
		}
	}
	
	public List<ApplicationUser> getAllUsers() {
		LOGGER.info("Entry getAllUsers::");
		List<ApplicationUser> userInfoList = null;
		try {

			userInfoList = jdbcTemplate.query(QueryConstantSecurity.ALL_USER_DTL, new ApplicationUserRowMapper());
			LOGGER.info("After Query getAllUsers ::");

		} catch (Exception e) {
			LOGGER.error("Exception in UserDetailsDAO :: getAllUsers ", e);
		}
		LOGGER.info("Exit getAllUsers::");
		return ListUtils.emptyIfNull(userInfoList);
	}
	
	public void deactivateUsers(List<String> userIds, String updatedBy) {
		
		for(String userId : userIds) {
			Object param[]= {ApplicationConstant.USER_STATUS_INACTIVE,updatedBy,CommonUtil.currentUtcTimestamp(),userId};			
			jdbcTemplate.update(QueryConstantSecurity.USER_UPDATE_STATUS_INACTIVE, param);
		}	
	}
	
	public void activateUsers(List<String> userIds, String updatedBy) {
		
		for(String userId : userIds) {
			Object param[]= {ApplicationConstant.USER_STATUS_ACTIVE,updatedBy,CommonUtil.currentUtcTimestamp(),userId};			
			jdbcTemplate.update(QueryConstantSecurity.USER_UPDATE_STATUS_ACTIVE, param);
		}	
	}
	
	public List<ApplicationUser> getUserByUseIdAndPassword(String userId, String password) {
		LOGGER.info("Entry getUserByUseIdAndPassword::");
		List<ApplicationUser> userInfoList = null;
		Object[] params = new Object[] {userId, password};
		try {

			userInfoList = jdbcTemplate.query(QueryConstantSecurity.USER_DTL_BY_USER_ID_AND_PASSWORD, new ApplicationUserRowMapper(), params);
			LOGGER.info("After Query Execution for getUserByUseIdAndPassword ::");

		} catch (Exception e) {
			LOGGER.error("Exception in UserDetailsDAO :: getUserByUseIdAndPassword ", e);
		}
		LOGGER.info("Exit getUserByUseIdAndPassword::");
		return ListUtils.emptyIfNull(userInfoList);
	}
	
	public void changePassword(String userId, String password) {
		
		Object param[]= {password,CommonUtil.addDaysToCurrentUtcTimestampSql(Long.parseLong(env.getProperty("passwordValidityInDays"))),
				userId,CommonUtil.currentUtcTimestamp(),userId};			
		jdbcTemplate.update(QueryConstantSecurity.USER_UPDATE_PASSWORD, param);
	}
	
	public List<ApplicationUser> getUserByUseIdAndStatus(String userId, String statusCode) {
		LOGGER.info("Entry getUserByUseIdAndStatus::");
		List<ApplicationUser> userInfoList = null;
		Object[] params = new Object[] {userId, statusCode};
		try {

			userInfoList = jdbcTemplate.query(QueryConstantSecurity.USER_DTL_BY_USER_ID_AND_STATUS_CODE, new ApplicationUserRowMapper(), params);
			LOGGER.info("After Query Execution for getUserByUseIdAndStatus ::");

		} catch (Exception e) {
			LOGGER.error("Exception in UserDetailsDAO :: getUserByUseIdAndStatus ", e);
		}
		LOGGER.info("Exit getUserByUseIdAndStatus::");
		return ListUtils.emptyIfNull(userInfoList);
	}
	
	public void resetPassword(String userId, String password, String updatedByUserId) {
		
		Object param[]= {password,CommonUtil.addDaysToCurrentUtcTimestampSql(Long.parseLong(env.getProperty("initialPasswordValidityInDays"))),
				updatedByUserId,CommonUtil.currentUtcTimestamp(),userId};			
		jdbcTemplate.update(QueryConstantSecurity.USER_UPDATE_PASSWORD, param);
	}
	
	public void addApplicationUsersAuditLog(String userId, String authorities, String accessUri, Timestamp accessDate) {
		
		Object param[]= {userId,authorities,accessUri,accessDate};			
		jdbcTemplate.update(QueryConstantSecurity.INSERT_USERS_AUDIT_LOG, param);
	}
	
	public List<ApplicationUser> getAllReadOnlyUsers() {
		LOGGER.info("Entry getAllReadOnlyUsers::");
		List<ApplicationUser> userInfoList = null;
		try {

			userInfoList = jdbcTemplate.query(QueryConstantSecurity.ALL_READ_ONLY_USER, new ReadOnlyUserRowMapper());
			LOGGER.info("After Query getAllReadOnlyUsers ::");

		} catch (Exception e) {
			LOGGER.error("Exception in UserDetailsDAO :: getAllReadOnlyUsers ", e);
		}
		LOGGER.info("Exit getAllReadOnlyUsers::");
		return ListUtils.emptyIfNull(userInfoList);
	}
	
	public List<String> allAuthorities() {

		List<String> names = null;
		try {
			names = jdbcTemplate.queryForList(QueryConstantSecurity.ALL_AUTHORITIES, String.class);
		} catch (Exception ex) {
			LOGGER.error("Exception occured in allAuthorities", ex);
		}
		if(CollectionUtils.isEmpty(names)) {
			names = new ArrayList<String>();
		}
		return names;
	}
}
