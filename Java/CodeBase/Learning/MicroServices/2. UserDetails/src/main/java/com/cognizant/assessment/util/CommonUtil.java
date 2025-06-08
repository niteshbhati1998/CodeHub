package com.cognizant.assessment.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommonUtil {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
