package com.smit.orbisIn.userService.producer;

import com.smit.orbisIn.userService.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.topic.user:user_topic}")
    private String userTopic;

    public void sendUserCreated(UserCreatedEvent event) {
        try {
            kafkaTemplate.send(userTopic, String.valueOf(event.getUserId()), objectMapper.writeValueAsString(event));
            log.info("Published UserCreatedEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for userId: {}", event.getUserId(), e);
        }
    }
}
