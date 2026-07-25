package com.shopcard.notification.controller;

import com.shopcard.notification.domain.model.Notification;
import com.shopcard.notification.domain.ports.in.GetNotificationsUseCase;
import com.shopcard.notification.domain.ports.in.MarkNotificationReadUseCase;
import com.shopcard.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@RequestHeader("X-User-Id") String userId) {
        List<Notification> notifications = getNotificationsUseCase.getNotifications(UUID.fromString(userId));
        List<NotificationResponse> responses = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        markNotificationReadUseCase.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .timestamp(notification.getTimestamp())
                .build();
    }
}
