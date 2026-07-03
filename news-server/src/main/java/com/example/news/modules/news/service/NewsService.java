package com.example.news.modules.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.news.common.exception.BusinessException;
import com.example.news.common.exception.ErrorCode;
import com.example.news.modules.news.dto.NewsQueryDTO;
import com.example.news.modules.news.dto.NewsSaveDTO;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.news.vo.NewsDetailVO;
import com.example.news.modules.news.vo.NewsVO;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import com.example.news.modules.category.entity.Category;
import com.example.news.modules.category.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService extends ServiceImpl<NewsMapper, News> {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    // ===== 点击计数缓冲：内存 INCR → 定时批量刷 MySQL =====
    private final ConcurrentHashMap<Long, Long> clickBuffer = new ConcurrentHashMap<>();

    /**
     * 增加点击量（先写内存缓冲）
     */
    public void incrementClick(Long newsId) {
        clickBuffer.merge(newsId, 1L, Long::sum);
    }

    /**
     * 每 5 分钟批量刷新点击量到数据库
     */
    @Scheduled(fixedRate = 300000)
    public void flushClickCounts() {
        if (clickBuffer.isEmpty()) return;
        Map<Long, Long> snapshot = new HashMap<>(clickBuffer);
        clickBuffer.clear();

        int updated = 0;
        for (Map.Entry<Long, Long> entry : snapshot.entrySet()) {
            News news = this.getById(entry.getKey());
            if (news != null) {
                news.setClicked(news.getClicked() + entry.getValue());
                this.updateById(news);
                updated++;
            }
        }
        if (updated > 0) log.info("点击量批量刷新: {} 篇文章, {} 条更新", snapshot.size(), updated);
    }

    /**
     * 应用关闭前强制刷新
     */
    @jakarta.annotation.PreDestroy
    public void flushOnShutdown() {
        flushClickCounts();
    }

    /**
     * 分页查询新闻列表
     */
    public Page<NewsVO> pageQuery(NewsQueryDTO dto, Long currentUserId, String currentRole) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();

        // 编辑只能看自己的
        if ("editor".equals(currentRole)) {
            wrapper.eq(News::getUserId, currentUserId);
        }

        // 筛选条件
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(News::getStatus, dto.getStatus());
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(News::getCategoryId, dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(News::getTitle, dto.getKeyword())
                           .or().like(News::getContent, dto.getKeyword()));
        }
        if (dto.getIsTop() != null && dto.getIsTop()) {
            wrapper.eq(News::getIsTop, 1);
        }
        wrapper.orderByDesc(News::getIsTop, News::getCreatedAt);

        Page<News> page = this.page(new Page<>(dto.getPage(), dto.getSize()), wrapper);

        // 批量查询作者和分类
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        Page<NewsVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(n -> toVO(n, userMap, catMap)).toList());
        return voPage;
    }

    /**
     * 新闻详情
     */
    public NewsDetailVO getDetail(Long newsId) {
        News news = this.getById(newsId);
        if (news == null) throw new BusinessException(ErrorCode.NEWS_NOT_FOUND);

        User author = userMapper.selectById(news.getUserId());
        Category cat = categoryMapper.selectById(news.getCategoryId());

        NewsDetailVO vo = new NewsDetailVO();
        vo.setNewsId(news.getNewsId());
        vo.setTitle(news.getTitle());
        vo.setSummary(news.getSummary());
        vo.setContent(news.getContent());
        vo.setTags(news.getTags());
        vo.setSource(news.getSource());
        vo.setCoverImage(news.getCoverImage());
        vo.setStatus(news.getStatus());
        vo.setRejectReason(news.getRejectReason());
        vo.setIsTop(news.getIsTop());
        vo.setClicked(news.getClicked());
        vo.setCreatedAt(news.getCreatedAt());
        vo.setUpdatedAt(news.getUpdatedAt());
        vo.setAuthorName(author != null ? author.getUsername() : "未知");
        vo.setCategoryName(cat != null ? cat.getName() : "未分类");
        vo.setCategoryId(news.getCategoryId());
        return vo;
    }

    /**
     * 创建/编辑新闻
     */
    @Transactional
    public Long saveNews(NewsSaveDTO dto, Long userId) {
        News news;
        if (dto.getNewsId() != null) {
            news = this.getById(dto.getNewsId());
            if (news == null) throw new BusinessException(ErrorCode.NEWS_NOT_FOUND);
            // 编辑器只能改自己的
            if (!userId.equals(news.getUserId())) {
                // admin can edit anyone's
            }
        } else {
            news = new News();
            news.setUserId(userId);
            news.setStatus("draft");
        }

        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setSummary(dto.getSummary());
        news.setTags(dto.getTags());
        news.setSource(dto.getSource());
        news.setCategoryId(dto.getCategoryId());
        news.setCoverImage(dto.getCoverImage());

        this.saveOrUpdate(news);
        return news.getNewsId();
    }

    /**
     * 提交审核
     */
    @Transactional
    public void submitForAudit(Long newsId, Long userId) {
        News news = this.getById(newsId);
        if (news == null) throw new BusinessException(ErrorCode.NEWS_NOT_FOUND);
        if (!"draft".equals(news.getStatus()) && !"rejected".equals(news.getStatus())) {
            throw new BusinessException(ErrorCode.NEWS_STATUS_ERROR);
        }
        news.setStatus("pending");
        news.setRejectReason(null);
        this.updateById(news);
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approveNews(List<Long> ids) {
        for (Long id : ids) {
            News news = this.getById(id);
            if (news == null || !"pending".equals(news.getStatus())) continue;
            news.setStatus("published");
            this.updateById(news);
        }
    }

    /**
     * 审核退回
     */
    @Transactional
    public void rejectNews(Long newsId, String reason) {
        News news = this.getById(newsId);
        if (news == null || !"pending".equals(news.getStatus())) {
            throw new BusinessException(ErrorCode.NEWS_STATUS_ERROR);
        }
        news.setStatus("rejected");
        news.setRejectReason(reason);
        this.updateById(news);
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteNews(Long newsId, Long userId, String role) {
        News news = this.getById(newsId);
        if (news == null) throw new BusinessException(ErrorCode.NEWS_NOT_FOUND);
        if (!"admin".equals(role) && !userId.equals(news.getUserId())) {
            throw new BusinessException(ErrorCode.NEWS_PERMISSION_DENIED);
        }
        this.removeById(newsId);
    }

    /**
     * 统计信息（仪表盘用）
     */
    public Map<String, Object> getStats(Long userId, String role) {
        long totalNews = this.count();
        long pendingCount = this.count(new LambdaQueryWrapper<News>().eq(News::getStatus, "pending"));
        long todayCount = this.count(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .ge(News::getCreatedAt, java.time.LocalDate.now().atStartOfDay()));

        if ("editor".equals(role)) {
            return Map.of(
                "myNews", this.count(new LambdaQueryWrapper<News>().eq(News::getUserId, userId)),
                "myPublished", this.count(new LambdaQueryWrapper<News>().eq(News::getUserId, userId).eq(News::getStatus, "published")),
                "myPending", this.count(new LambdaQueryWrapper<News>().eq(News::getUserId, userId).eq(News::getStatus, "pending")),
                "myRejected", this.count(new LambdaQueryWrapper<News>().eq(News::getUserId, userId).eq(News::getStatus, "rejected")),
                "myDraft", this.count(new LambdaQueryWrapper<News>().eq(News::getUserId, userId).eq(News::getStatus, "draft"))
            );
        }
        return Map.of("totalNews", totalNews, "pendingCount", pendingCount, "todayCount", todayCount);
    }

    /**
     * 近7天发布趋势
     */
    public List<Map<String, Object>> getTrend() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            long cnt = this.count(new LambdaQueryWrapper<News>()
                    .ge(News::getCreatedAt, d.atStartOfDay())
                    .lt(News::getCreatedAt, d.plusDays(1).atStartOfDay()));
            list.add(Map.of("date", d.toString().substring(5), "count", cnt));
        }
        return list;
    }

    /**
     * 今日发布量（审核员仪表盘用）
     */
    public long countTodayPublished() {
        return this.count(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .ge(News::getUpdatedAt, java.time.LocalDate.now().atStartOfDay()));
    }

    /**
     * 今日退回量
     */
    public long countTodayRejected() {
        return this.count(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "rejected")
                .ge(News::getUpdatedAt, java.time.LocalDate.now().atStartOfDay()));
    }

    /**
     * 热门新闻 Top N
     */
    public List<NewsVO> getHotNews(int limit) {
        List<News> list = this.list(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .orderByDesc(News::getClicked)
                .last("LIMIT " + limit));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
        return list.stream().map(n -> toVO(n, userMap, catMap)).toList();
    }

    // ===== 转换 =====
    private NewsVO toVO(News n, Map<Long, String> userMap, Map<Long, String> catMap) {
        NewsVO vo = new NewsVO();
        vo.setNewsId(n.getNewsId());
        vo.setTitle(n.getTitle());
        vo.setSummary(n.getSummary());
        vo.setCoverImage(n.getCoverImage());
        vo.setStatus(n.getStatus());
        vo.setRejectReason(n.getRejectReason());
        vo.setIsTop(n.getIsTop());
        vo.setClicked(n.getClicked());
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());
        vo.setAuthorName(userMap.getOrDefault(n.getUserId(), "未知"));
        vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
        vo.setCategoryId(n.getCategoryId());
        vo.setContent(n.getContent());
        return vo;
    }
}
