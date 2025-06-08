package com.cognizant.assessment.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cognizant.assessment.model.User;

@Service
public class UserService {
	
	@Autowired
	RestTemplate restTemplate;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	public User getUserDetails(int userId) {
		User user = null;
		try {
			List<User> list = new ArrayList<>();
			list.add(new User(1,"nitesh"));
			list.add(new User(2,"sahil"));
			list.add(new User(3,"bhola"));

		    user = list.stream().filter(n->n.getUserId()==userId).findFirst().get();
		    
		    //String contact = restTemplate.getForObject("http://localhost:7002/user/contact/"+userId, String.class);
		    String contact = restTemplate.getForObject("http://UserContactDetails/user/contact/"+userId, String.class);
		    
		    user.setContact(contact);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getUserDetails Service "+ex.getMessage());
		}
		return user;
	}
}
