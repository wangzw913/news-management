package com.example.news.modules.favorite.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.news.common.response.PageResult;
import com.example.news.common.response.Result;
import com.example.news.modules.favorite.entity.Favorite;
import com.example.news.modules.favorite.mapper.FavoriteMapper;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import com.example.news.modules.category.entity.Category;
import com.example.news.modules.category.mapper.CategoryMapper;
import com.example.news.modules.news.vo.NewsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "收藏", description = "新闻收藏")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;
    private final NewsMapper newsMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "我的收藏列表")
    @GetMapping
    public Result<PageResult<NewsVO>> myFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Page<Favorite> p = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreatedAt));

        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        var records = p.getRecords().stream().map(f -> {
            News n = newsMapper.selectById(f.getNewsId());
            if (n == null) return null;
            NewsVO vo = new NewsVO();
            vo.setNewsId(n.getNewsId());
            vo.setTitle(n.getTitle());
            vo.setSummary(n.getSummary());
            vo.setCoverImage(n.getCoverImage());
            vo.setStatus(n.getStatus());
            vo.setClicked(n.getClicked());
            vo.setCreatedAt(n.getCreatedAt());
            vo.setAuthorName(userMap.getOrDefault(n.getUserId(), "未知"));
            vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
            vo.setCategoryId(n.getCategoryId());
            return vo;
        }).filter(v -> v != null).toList();

        return Result.ok(PageResult.of(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @Operation(summary = "切换收藏状态")
    @PostMapping("/toggle/{newsId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long newsId, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Favorite fav = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getNewsId, newsId));
        if (fav != null) {
            favoriteMapper.deleteById(fav.getFavId());
            return Result.ok(Map.of("favorited", false));
        } else {
            Favorite nf = new Favorite();
            nf.setUserId(userId);
            nf.setNewsId(newsId);
            favoriteMapper.insert(nf);
            return Result.ok(Map.of("favorited", true));
        }
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{newsId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long newsId, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        boolean favorited = favoriteMapper.exists(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getNewsId, newsId));
        return Result.ok(Map.of("favorited", favorited));
    }
}
