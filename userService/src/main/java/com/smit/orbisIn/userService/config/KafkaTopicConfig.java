package com.smit.orbisIn.userService.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.user:user_topic}")
    private String userTopic;

    @Bean
    public NewTopic userTopic() {
        return new NewTopic(userTopic, 1, (short) 1);
    }
}
