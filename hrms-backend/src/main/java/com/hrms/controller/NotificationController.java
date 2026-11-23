package com.hrms.controller;

import com.hrms.security.CustomUserDetails;
import com.hrms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notifications and read receipt management")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications and unread count")
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "false") boolean unread,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userDetails.getId(), unread, limit));
    }

    @PatchMapping
    @Operation(summary = "Mark single or all notifications as read")
    public ResponseEntity<?> markAsRead(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (Boolean.TRUE.equals(body.get("markAllRead"))) {
            notificationService.markAllRead(userDetails.getId());
        } else if (body.get("id") != null) {
            notificationService.markRead((String) body.get("id"), userDetails.getId());
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
