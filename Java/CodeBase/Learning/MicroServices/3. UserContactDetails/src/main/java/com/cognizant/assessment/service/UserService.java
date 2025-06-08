package com.cognizant.assessment.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cognizant.assessment.model.User;

@Service
public class UserService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	public String getUserContactDetails(int userId) {
		String contact = null;
		try {
			List<User> list = new ArrayList<>();
			list.add(new User(1,"123"));
			list.add(new User(2,"456"));
			list.add(new User(3,"789"));

			contact = list.stream().filter(n->n.getUserId()==userId).findFirst().get().getContactInfo();
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getUserDetails Service "+ex.getMessage());
		}
		return contact;
	}
}
