package com.smit.orbisIn.connectionsService.producer;

import com.smit.orbisIn.connectionsService.event.ConnectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.topic.connection:connection_topic}")
    private String connectionTopic;

    public void sendConnectionEvent(ConnectionEvent event) {
        try {
            kafkaTemplate.send(connectionTopic, String.valueOf(event.getSenderId()), objectMapper.writeValueAsString(event));
            log.info("Published ConnectionEvent senderId: {} receiverId: {} status: {}",
                    event.getSenderId(), event.getReceiverId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish ConnectionEvent senderId: {}", event.getSenderId(), e);
        }
    }
}
