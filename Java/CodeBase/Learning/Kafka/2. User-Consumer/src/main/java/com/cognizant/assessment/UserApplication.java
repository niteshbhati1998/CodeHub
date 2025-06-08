package com.cognizant.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserApplication {
	
	public static void main(String[] args) {
		try {	
			SpringApplication.run(UserApplication.class, args);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
