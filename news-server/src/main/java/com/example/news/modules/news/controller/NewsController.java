package com.example.news.modules.news.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.news.common.annotation.LogOperation;
import com.example.news.common.exception.BusinessException;
import com.example.news.common.exception.ErrorCode;
import com.example.news.common.response.PageResult;
import com.example.news.common.response.Result;
import com.example.news.modules.news.dto.NewsQueryDTO;
import com.example.news.modules.news.dto.NewsSaveDTO;
import com.example.news.modules.news.service.NewsService;
import com.example.news.modules.news.vo.NewsDetailVO;
import com.example.news.modules.news.vo.NewsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "新闻管理", description = "新闻 CRUD、审核")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "新闻列表")
    @GetMapping("/news")
    public Result<PageResult<NewsVO>> list(NewsQueryDTO dto, Authentication auth) {
        String role = getRole(auth);
        Long userId = getUserId(auth);
        Page<NewsVO> page = newsService.pageQuery(dto, userId, role);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "新闻详情（含点击计数）")
    @GetMapping("/news/{id}")
    public Result<NewsDetailVO> detail(@PathVariable Long id) {
        newsService.incrementClick(id); // 内存缓冲计数，定时刷库
        return Result.ok(newsService.getDetail(id));
    }

    @Operation(summary = "创建新闻")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','USER')")
    @LogOperation(value = "创建新闻")
    @PostMapping("/news")
    public Result<Map<String, Long>> create(@Valid @RequestBody NewsSaveDTO dto, Authentication auth) {
        Long userId = getUserId(auth);
        Long newsId = newsService.saveNews(dto, userId);
        return Result.ok(Map.of("newsId", newsId));
    }

    @Operation(summary = "编辑新闻")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','USER')")
    @LogOperation(value = "编辑新闻")
    @PutMapping("/news/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody NewsSaveDTO dto, Authentication auth) {
        dto.setNewsId(id);
        newsService.saveNews(dto, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "提交审核")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','USER')")
    @LogOperation(value = "提交审核")
    @PutMapping("/news/{id}/submit")
    public Result<Void> submit(@PathVariable Long id, Authentication auth) {
        newsService.submitForAudit(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "删除新闻")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','USER')")
    @LogOperation(value = "删除新闻")
    @DeleteMapping("/news/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        newsService.deleteNews(id, getUserId(auth), getRole(auth));
        return Result.ok();
    }

    @Operation(summary = "批量删除（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "批量删除新闻")
    @PostMapping("/admin/news/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(id -> newsService.removeById(id));
        return Result.ok();
    }

    @Operation(summary = "热门新闻")
    @GetMapping("/news/hot")
    public Result<List<NewsVO>> hotNews(@RequestParam(defaultValue = "6") int limit) {
        return Result.ok(newsService.getHotNews(limit));
    }

    // ===== helpers =====
    private String getRole(Authentication auth) {
        if (auth == null || auth.getAuthorities().isEmpty()) return "anonymous";
        String authStr = auth.getAuthorities().iterator().next().getAuthority();
        return authStr.replace("ROLE_", "").toLowerCase();
    }

    private Long getUserId(Authentication auth) {
        if (auth == null) return 0L;
        return (Long) auth.getCredentials();
    }
}
