package com.cognizant.lineage.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsServiceImpl implements UserDetailsService {

	private static Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsServiceImpl.class);

	@Autowired
	UserDetailsDAO userDetailsDAO;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		LOGGER.info("Entry loadUserByUsername::");
		List<ApplicationUser> userInfoList = null;
		List<ApplicationAuthoritie> authoritieList = null;
		ApplicationUser user = null;
		User springUser = null;

		userInfoList = userDetailsDAO.getUserByUseId(userId);
		authoritieList = userDetailsDAO.getAuthoritieByUseId(userId);
		if (userInfoList.isEmpty()) {
			throw new UsernameNotFoundException("User with userId: " + userId + " not found");
		}
		user = userInfoList.get(0);
		Set<GrantedAuthority> ga = new HashSet<>();
		for (ApplicationAuthoritie authoritie : authoritieList) {
			ga.add(new SimpleGrantedAuthority(authoritie.getAuthority()));
		}

		springUser = new User(user.getUserId(), user.getPassword(), ga);
		
		checkMaxUsersAandApplicationLicenseExpire();

		LOGGER.info("Exit loadUserByUsername::");
		return springUser;
	}
	
	/**
	 * This code was added to prevent authentication if the user table 
	 * contains more that ApplicationConstant.MAXUSER users.
	 */
	public void checkMaxUsersAandApplicationLicenseExpire() {
		
		if( CommonUtil.isApplicationLicenseExpired()) {
			
			throw new DataScanException(ApplicationConstant.APPLICATION_LICENSE_EXPIRY_MSG);
		}
		
		List<ApplicationUser> allUserInfoList = userDetailsDAO.getAllUsers();
		if( allUserInfoList.size() > ApplicationConstant.MAXUSER) {
			
			throw new DataScanException(ApplicationConstant.MAXUSER_MSG);
		}
	}
}
