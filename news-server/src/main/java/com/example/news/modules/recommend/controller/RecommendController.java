package com.example.news.modules.recommend.controller;

import com.example.news.common.response.Result;
import com.example.news.modules.news.vo.NewsVO;
import com.example.news.modules.recommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "内容推荐", description = "基于 TF-IDF + 余弦相似度的内容推荐")
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @Operation(summary = "相关文章推荐（内容相似度）")
    @GetMapping("/similar/{newsId}")
    public Result<List<NewsVO>> similarNews(@PathVariable Long newsId,
                                            @RequestParam(defaultValue = "5") int limit) {
        return Result.ok(recommendService.getSimilarNews(newsId, limit));
    }

    @Operation(summary = "个性化推荐（用户维度）")
    @GetMapping("/for-you")
    public Result<List<NewsVO>> forYou(Authentication auth,
                                       @RequestParam(defaultValue = "5") int limit) {
        Long userId = (Long) auth.getCredentials();
        return Result.ok(recommendService.getRecommendedForUser(userId, limit));
    }
}
