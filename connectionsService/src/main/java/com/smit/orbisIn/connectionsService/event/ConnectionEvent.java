package com.smit.orbisIn.connectionsService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionEvent {
    private Long senderId;
    private Long receiverId;
    private String status;
}
