package com.smit.orbisIn.notificationService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLikedEvent {
    private Long postId;
    private Long likerUserId;
    private Long postOwnerUserId;
}
