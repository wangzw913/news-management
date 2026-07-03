package com.example.news.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.news.common.response.PageResult;
import com.example.news.common.response.Result;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.news.vo.NewsVO;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import com.example.news.modules.category.entity.Category;
import com.example.news.modules.category.mapper.CategoryMapper;
import com.example.news.modules.like.entity.Like;
import com.example.news.modules.like.mapper.LikeMapper;
import com.example.news.modules.favorite.entity.Favorite;
import com.example.news.modules.favorite.mapper.FavoriteMapper;
import com.example.news.modules.history.entity.ReadHistory;
import com.example.news.modules.history.mapper.ReadHistoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "个人中心", description = "收藏/点赞/历史/投稿/统计")
@RestController
@RequestMapping("/api/v1/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final NewsMapper newsMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;
    private final ReadHistoryMapper historyMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "我的统计")
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return Result.ok(Map.of(
            "favorites", favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)),
            "likes", likeMapper.selectCount(new LambdaQueryWrapper<Like>().eq(Like::getUserId, userId)),
            "submissions", newsMapper.selectCount(new LambdaQueryWrapper<News>().eq(News::getUserId, userId)),
            "history", historyMapper.selectCount(new LambdaQueryWrapper<ReadHistory>().eq(ReadHistory::getUserId, userId))
        ));
    }

    @Operation(summary = "我的投稿列表")
    @GetMapping("/submissions")
    public Result<PageResult<NewsVO>> submissions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<News>()
                .eq(News::getUserId, userId);
        if (status != null && !status.isEmpty()) wrapper.eq(News::getStatus, status);
        wrapper.orderByDesc(News::getCreatedAt);

        Page<News> p = newsMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        var records = p.getRecords().stream().map(n -> {
            NewsVO vo = new NewsVO();
            vo.setNewsId(n.getNewsId());
            vo.setTitle(n.getTitle());
            vo.setStatus(n.getStatus());
            vo.setRejectReason(n.getRejectReason());
            vo.setClicked(n.getClicked());
            vo.setCreatedAt(n.getCreatedAt());
            vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
            return vo;
        }).toList();

        return Result.ok(PageResult.of(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @Operation(summary = "我的点赞列表")
    @GetMapping("/likes")
    public Result<PageResult<NewsVO>> myLikes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Page<Like> p = likeMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Like>().eq(Like::getUserId, userId).orderByDesc(Like::getCreatedAt));

        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        var records = p.getRecords().stream().map(l -> {
            News n = newsMapper.selectById(l.getNewsId());
            if (n == null) return null;
            NewsVO vo = new NewsVO();
            vo.setNewsId(n.getNewsId());
            vo.setTitle(n.getTitle());
            vo.setClicked(n.getClicked());
            vo.setCreatedAt(n.getCreatedAt());
            vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
            return vo;
        }).filter(v -> v != null).toList();

        return Result.ok(PageResult.of(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @Operation(summary = "浏览历史")
    @GetMapping("/history")
    public Result<PageResult<NewsVO>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Page<ReadHistory> p = historyMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ReadHistory>().eq(ReadHistory::getUserId, userId).orderByDesc(ReadHistory::getCreatedAt));

        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        var records = p.getRecords().stream().map(h -> {
            News n = newsMapper.selectById(h.getNewsId());
            if (n == null) return null;
            NewsVO vo = new NewsVO();
            vo.setNewsId(n.getNewsId());
            vo.setTitle(n.getTitle());
            vo.setClicked(n.getClicked());
            vo.setCreatedAt(n.getCreatedAt());
            vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
            return vo;
        }).filter(v -> v != null).toList();

        return Result.ok(PageResult.of(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }
}
