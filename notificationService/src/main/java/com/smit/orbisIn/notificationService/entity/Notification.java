package com.smit.orbisIn.notificationService.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    String id;

    Long recipientId;
    Long senderId;
    String type;
    String message;
    Long postId;
    Boolean read = false;

    @CreatedDate
    LocalDateTime createdAt;
}
