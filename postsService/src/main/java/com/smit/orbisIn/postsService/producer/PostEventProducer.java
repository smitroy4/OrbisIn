package com.smit.orbisIn.postsService.producer;

import com.smit.orbisIn.postsService.event.PostCreatedEvent;
import com.smit.orbisIn.postsService.event.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.topic.post:post_topic}")
    private String postTopic;

    public void sendPostCreated(PostCreatedEvent event) {
        try {
            kafkaTemplate.send(postTopic, String.valueOf(event.getPostId()), objectMapper.writeValueAsString(event));
            log.info("Published PostCreatedEvent for postId: {}", event.getPostId());
        } catch (Exception e) {
            log.error("Failed to publish PostCreatedEvent for postId: {}", event.getPostId(), e);
        }
    }

    public void sendPostLiked(PostLikedEvent event) {
        try {
            kafkaTemplate.send(postTopic, String.valueOf(event.getPostId()), objectMapper.writeValueAsString(event));
            log.info("Published PostLikedEvent for postId: {} by user: {}", event.getPostId(), event.getLikerUserId());
        } catch (Exception e) {
            log.error("Failed to publish PostLikedEvent for postId: {}", event.getPostId(), e);
        }
    }
}
