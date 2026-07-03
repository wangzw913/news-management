package com.example.news.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.notification.entity.Notification;
import com.example.news.modules.notification.handler.NotificationWebSocketHandler;
import com.example.news.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务 — 异步推送 + 落库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler wsHandler;

    /**
     * 稿件审核通过通知
     */
    @Async
    public void notifyApproved(News news) {
        notifyUser(news.getUserId(), "approved",
                "稿件审核通过", "你的稿件「" + news.getTitle() + "」已通过审核并发布", news.getNewsId());
    }

    /**
     * 稿件退回通知
     */
    @Async
    public void notifyRejected(News news) {
        String reason = news.getRejectReason() != null ? "原因：" + news.getRejectReason() : "";
        notifyUser(news.getUserId(), "rejected",
                "稿件被退回", "你的稿件「" + news.getTitle() + "」已被退回。" + reason, news.getNewsId());
    }

    /**
     * 新评论通知
     */
    @Async
    public void notifyNewComment(News news, String commenterName) {
        notifyUser(news.getUserId(), "comment",
                "新评论", commenterName + " 评论了你的稿件「" + news.getTitle() + "」", news.getNewsId());
    }

    private void notifyUser(Long userId, String type, String title, String content, Long targetId) {
        // 1. 落库
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setContent(content);
        notif.setTargetId(targetId);
        notificationMapper.insert(notif);

        // 2. WebSocket 实时推送
        wsHandler.sendNotification(userId, type, title, content, targetId);
    }

    /**
     * 获取用户未读通知
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT 20"));
    }

    /**
     * 获取未读数量
     */
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    /**
     * 标记已读
     */
    @Transactional
    public void markRead(Long notifId) {
        Notification n = notificationMapper.selectById(notifId);
        if (n != null) { n.setIsRead(1); notificationMapper.updateById(n); }
    }
}
