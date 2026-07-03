package com.example.news.modules.notification.controller;

import com.example.news.common.response.Result;
import com.example.news.modules.notification.entity.Notification;
import com.example.news.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "通知", description = "实时通知（WebSocket + 轮询）")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "未读通知列表")
    @GetMapping("/unread")
    public Result<List<Notification>> unread(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return Result.ok(notificationService.getUnreadNotifications(userId));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return Result.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }
}
