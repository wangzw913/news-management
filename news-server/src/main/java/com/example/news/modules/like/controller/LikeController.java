package com.example.news.modules.like.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.common.response.Result;
import com.example.news.modules.like.entity.Like;
import com.example.news.modules.like.mapper.LikeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "点赞", description = "新闻点赞")
@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeMapper likeMapper;

    @Operation(summary = "切换点赞状态")
    @PostMapping("/toggle/{newsId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long newsId, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Like like = likeMapper.selectOne(new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, userId).eq(Like::getNewsId, newsId));
        if (like != null) {
            likeMapper.deleteById(like.getLikeId());
            return Result.ok(Map.of("liked", false));
        } else {
            Like nl = new Like();
            nl.setUserId(userId);
            nl.setNewsId(newsId);
            likeMapper.insert(nl);
            return Result.ok(Map.of("liked", true));
        }
    }

    @Operation(summary = "检查是否已点赞")
    @GetMapping("/check/{newsId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long newsId, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        boolean liked = likeMapper.exists(new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, userId).eq(Like::getNewsId, newsId));
        return Result.ok(Map.of("liked", liked));
    }

    @Operation(summary = "点赞数")
    @GetMapping("/count/{newsId}")
    public Result<Map<String, Long>> count(@PathVariable Long newsId) {
        long cnt = likeMapper.selectCount(new LambdaQueryWrapper<Like>().eq(Like::getNewsId, newsId));
        return Result.ok(Map.of("count", cnt));
    }
}
