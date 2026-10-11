package com.smit.orbisIn.notificationService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreatedEvent {
    private Long postId;
    private Long creatorId;
    private String content;
}
