package com.example.assessment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.assessment.model.JwtRequest;
import com.example.assessment.model.JwtResponse;
import com.example.assessment.security.JwtHelper;
import com.example.assessment.service.JwtService;

@RestController
@RequestMapping("/authentication")
public class JwtController {

	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JwtHelper jwtHelper;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtController.class);
	
	@PostMapping("/authenticate")
	public ResponseEntity<JwtResponse> getAuthToken(@RequestBody JwtRequest jwtRequest) {
		String username = jwtRequest.getUsername();
		String password = jwtRequest.getPassword();
		
		jwtService.doAuthenticate(username, password);
		
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String token = jwtHelper.generateToken(userDetails);
		
		JwtResponse jwtResponse = JwtResponse.builder()
				.token(token)
				.username(username)
				.build();		
		return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
	}	
}