package com.cognizant.assessment.util;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class CommonUtil {

	@Bean
	NewTopic topic() {
		return TopicBuilder.name(AppConstants.TOPIC_NAME)
				//.partitions(partitionCount)
				//.replicas(replicaCount)
				.build();
	}
}
