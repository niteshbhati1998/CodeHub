package com.cognizant.assessment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.assessment.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
			
	@GetMapping("/contact/{userId}")
	public String getUserContactDetails(@PathVariable int userId) {
		String contact = null;
		try {
			contact = userService.getUserContactDetails(userId);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getUserContactDetails Controller "+ex.getMessage());
		}
		return contact;
	}
}
