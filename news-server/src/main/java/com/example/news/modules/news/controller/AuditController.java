package com.example.news.modules.news.controller;

import com.example.news.common.annotation.LogOperation;
import com.example.news.common.response.Result;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.news.service.NewsService;
import com.example.news.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审核管理", description = "稿件审核通过/退回（含实时通知推送）")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final NewsService newsService;
    private final NewsMapper newsMapper;
    private final NotificationService notificationService;

    @Operation(summary = "审核通过")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @LogOperation(value = "审核通过")
    @PutMapping("/approve")
    public Result<Void> approve(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.getOrDefault("ids", List.of());
        newsService.approveNews(ids);
        // 异步推送通知
        for (Long id : ids) {
            News news = newsMapper.selectById(id);
            if (news != null) notificationService.notifyApproved(news);
        }
        return Result.ok();
    }

    @Operation(summary = "审核退回")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @LogOperation(value = "审核退回")
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        newsService.rejectNews(id, reason);
        News news = newsMapper.selectById(id);
        if (news != null) notificationService.notifyRejected(news);
        return Result.ok();
    }
}
