package com.example.news.modules.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.common.response.Result;
import com.example.news.common.utils.IpUtil;
import com.example.news.common.utils.ServletUtils;
import com.example.news.modules.review.entity.Review;
import com.example.news.modules.review.mapper.ReviewMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "评论", description = "新闻评论与回复")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewMapper reviewMapper;

    @Operation(summary = "文章评论列表（含嵌套回复）")
    @GetMapping("/news/{newsId}")
    public Result<List<Map<String, Object>>> list(@PathVariable Long newsId) {
        // 已审核的顶级评论
        List<Review> parents = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getNewsId, newsId)
                .eq(Review::getState, "approved")
                .isNull(Review::getParentId)
                .orderByDesc(Review::getCreatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Review p : parents) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("reviewId", p.getReviewId());
            item.put("username", p.getUsername());
            item.put("content", p.getContent());
            item.put("createdAt", p.getCreatedAt());

            // 子回复
            List<Review> replies = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                    .eq(Review::getParentId, p.getReviewId())
                    .orderByAsc(Review::getCreatedAt));
            item.put("replies", replies);
            result.add(item);
        }
        return Result.ok(result);
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> body, Authentication auth) {
        Review r = new Review();
        r.setNewsId(Long.valueOf(body.get("newsId").toString()));
        r.setContent(body.get("content").toString());
        if (body.containsKey("parentId") && body.get("parentId") != null) {
            r.setParentId(Long.valueOf(body.get("parentId").toString()));
        }
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            r.setUserId((Long) auth.getCredentials());
            r.setUsername(auth.getName());
        } else {
            r.setUsername(body.getOrDefault("username", "匿名").toString());
        }
        r.setIp(ServletUtils.getCurrentRequest().map(IpUtil::getClientIp).orElse(""));
        r.setState("approved");
        reviewMapper.insert(r);
        return Result.ok();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        reviewMapper.deleteById(id);
        return Result.ok();
    }
}
