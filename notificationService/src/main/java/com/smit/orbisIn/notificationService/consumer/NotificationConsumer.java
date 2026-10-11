package com.smit.orbisIn.notificationService.consumer;

import com.smit.orbisIn.notificationService.entity.Notification;
import com.smit.orbisIn.notificationService.event.ConnectionEvent;
import com.smit.orbisIn.notificationService.event.UserCreatedEvent;
import com.smit.orbisIn.notificationService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.topic.user:user_topic}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void handleUserCreated(String payload) {
        try {
            UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
            log.info("Received UserCreatedEvent for userId: {}", event.getUserId());
            Notification notification = new Notification();
            notification.setRecipientId(event.getUserId());
            notification.setType("WELCOME");
            notification.setMessage("Welcome " + event.getName() + "! Your account has been created.");
            notification.setRead(false);
            notificationRepository.save(notification);
            log.info("Saved WELCOME notification for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to handle user_topic payload: {}", payload, e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.post:post_topic}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void handlePostTopic(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            log.info("Received post_topic event: {}", event);
            // PostLikedEvent carries likerUserId + postOwnerUserId; PostCreatedEvent carries creatorId.
            if (!event.has("likerUserId")) {
                return;
            }
            Long postId = event.path("postId").isNull() ? null : event.path("postId").asLong();
            Long liker = event.path("likerUserId").isNull() ? null : event.path("likerUserId").asLong();
            Long owner = event.path("postOwnerUserId").isNull() ? null : event.path("postOwnerUserId").asLong();
            if (owner == null || owner.equals(liker)) {
                return;
            }
            Notification notification = new Notification();
            notification.setRecipientId(owner);
            notification.setSenderId(liker);
            notification.setType("POST_LIKED");
            notification.setMessage("User " + liker + " liked your post " + postId);
            notification.setPostId(postId);
            notification.setRead(false);
            notificationRepository.save(notification);
            log.info("Saved POST_LIKED notification for userId: {}", owner);
        } catch (Exception e) {
            log.error("Failed to handle post_topic payload: {}", payload, e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.connection:connection_topic}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void handleConnectionEvent(String payload) {
        try {
            ConnectionEvent event = objectMapper.readValue(payload, ConnectionEvent.class);
            log.info("Received ConnectionEvent senderId: {} receiverId: {} status: {}",
                    event.getSenderId(), event.getReceiverId(), event.getStatus());
            Notification notification = new Notification();
            notification.setRecipientId(event.getReceiverId());
            notification.setSenderId(event.getSenderId());
            notification.setType("CONNECTION_" + (event.getStatus() == null ? "UPDATE" : event.getStatus().toUpperCase()));
            notification.setMessage("Connection update from user " + event.getSenderId() + ": " + event.getStatus());
            notification.setRead(false);
            notificationRepository.save(notification);
            log.info("Saved CONNECTION notification for userId: {}", event.getReceiverId());
        } catch (Exception e) {
            log.error("Failed to handle connection_topic payload: {}", payload, e);
        }
    }
}
