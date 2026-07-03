package com.example.news.modules.news.controller;

import com.example.news.common.response.Result;
import com.example.news.modules.news.service.NewsService;
import com.example.news.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "仪表盘", description = "统计数据")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final NewsService newsService;
    private final UserService userService;

    @Operation(summary = "仪表盘统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(Authentication auth) {
        String role = getRole(auth);
        Long userId = getUserId(auth);

        Map<String, Object> result = new HashMap<>(newsService.getStats(userId, role));

        if ("admin".equals(role)) {
            result.put("totalUsers", userService.count());
        }
        if ("auditor".equals(role)) {
            result.put("todayPublished", newsService.countTodayPublished());
            result.put("todayRejected", newsService.countTodayRejected());
        }
        return Result.ok(result);
    }

    @Operation(summary = "近7天趋势")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend() {
        return Result.ok(newsService.getTrend());
    }

    private String getRole(Authentication auth) {
        if (auth == null || auth.getAuthorities().isEmpty()) return "anonymous";
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "").toLowerCase();
    }

    private Long getUserId(Authentication auth) {
        if (auth == null) return 0L;
        return (Long) auth.getCredentials();
    }
}
