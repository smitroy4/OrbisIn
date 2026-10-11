package com.smit.orbisIn.notificationService.controller;

import com.smit.orbisIn.notificationService.context.ContextHolder;
import com.smit.orbisIn.notificationService.entity.Notification;
import com.smit.orbisIn.notificationService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    private Long currentUserId() {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid user context");
        }
        return userId;
    }

    private void requireSelf(Long recipientId) {
        if (!currentUserId().equals(recipientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own notifications");
        }
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long recipientId) {
        requireSelf(recipientId);
        return ResponseEntity.ok(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId));
    }

    @GetMapping("/recipient/{recipientId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long recipientId) {
        requireSelf(recipientId);
        return ResponseEntity.ok(Map.of(
                "unreadCount", notificationRepository.countByRecipientIdAndReadFalse(recipientId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        Long userId = currentUserId();
        return notificationRepository.findById(id)
                .map(notification -> {
                    if (!userId.equals(notification.getRecipientId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You can only modify your own notifications");
                    }
                    notification.setRead(true);
                    return ResponseEntity.ok(notificationRepository.save(notification));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
