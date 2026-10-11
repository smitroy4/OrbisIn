package com.smit.orbisIn.connectionsService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConnectionRequestDto {
    private Long id;
    private Long senderUserId;
    private String senderName;
    private Long receiverUserId;
    private String receiverName;
    private String status;
    private LocalDateTime createdAt;
}
