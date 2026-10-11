package com.smit.orbisIn.connectionsService.consumer;

import com.smit.orbisIn.connectionsService.entity.Person;
import com.smit.orbisIn.connectionsService.event.UserCreatedEvent;
import com.smit.orbisIn.connectionsService.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.topic.user:user_topic}", groupId = "${spring.kafka.consumer.group-id:connections-service}")
    public void handleUserCreated(String payload) {
        try {
            UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
            log.info("Received UserCreatedEvent for userId: {}", event.getUserId());
            if (event.getUserId() == null) {
                log.warn("Ignoring UserCreatedEvent with null userId");
                return;
            }
            if (personRepository.findByUserId(event.getUserId()).isPresent()) {
                log.info("Person already exists for userId: {}, skipping", event.getUserId());
                return;
            }
            Person person = new Person();
            person.setUserId(event.getUserId());
            person.setName(event.getName());
            personRepository.save(person);
            log.info("Created Person node for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to handle UserCreatedEvent payload: {}", payload, e);
        }
    }
}
