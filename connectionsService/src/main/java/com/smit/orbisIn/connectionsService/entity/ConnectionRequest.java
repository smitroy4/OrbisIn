package com.smit.orbisIn.connectionsService.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionRequest {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";

    @Id
    @GeneratedValue
    Long id;

    Long senderUserId;

    Long receiverUserId;

    String status = PENDING;

    LocalDateTime createdAt = LocalDateTime.now();
}
