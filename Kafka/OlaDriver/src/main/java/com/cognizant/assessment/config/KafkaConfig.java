package com.cognizant.assessment.config;

import com.cognizant.assessment.constant.AppConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(AppConstants.TOPIC_DRIVER_LOCATION)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
