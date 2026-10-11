package com.smit.orbisIn.connectionsService.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.user:user_topic}")
    private String userTopic;

    @Value("${kafka.topic.connection:connection_topic}")
    private String connectionTopic;

    @Bean
    public NewTopic userTopic() {
        return new NewTopic(userTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic connectionTopic() {
        return new NewTopic(connectionTopic, 1, (short) 1);
    }
}
