package com.example.news.modules.news;

import com.example.news.common.exception.BusinessException;
import com.example.news.modules.category.mapper.CategoryMapper;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.news.service.NewsService;
import com.example.news.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("新闻服务单元测试")
class NewsServiceTest {

    @Mock private NewsMapper newsMapper;
    @Mock private UserMapper userMapper;
    @Mock private CategoryMapper categoryMapper;
    private NewsService newsService;
    private News mockNews;

    @BeforeEach
    void setUp() {
        newsService = new NewsService(userMapper, categoryMapper);
        ReflectionTestUtils.setField(newsService, "baseMapper", newsMapper);

        mockNews = new News();
        mockNews.setNewsId(1L);
        mockNews.setUserId(2L);
        mockNews.setCategoryId(1L);
        mockNews.setTitle("测试新闻");
        mockNews.setContent("这是一篇测试新闻的内容");
        mockNews.setStatus("draft");
        mockNews.setClicked(0L);
        mockNews.setCreatedAt(LocalDateTime.now());
        mockNews.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("提交审核 — 草稿可以提交")
    void shouldSubmitDraftForAudit() {
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        assertDoesNotThrow(() -> newsService.submitForAudit(1L, 2L));

        verify(newsMapper).updateById(any(News.class));
        assertEquals("pending", mockNews.getStatus());
    }

    @Test
    @DisplayName("提交审核失败 — 已发布稿件不可提交")
    void shouldNotSubmitPublished() {
        mockNews.setStatus("published");
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        assertThrows(BusinessException.class, () -> newsService.submitForAudit(1L, 2L));
    }

    @Test
    @DisplayName("审核通过")
    void shouldApproveNews() {
        mockNews.setStatus("pending");
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        assertDoesNotThrow(() -> newsService.approveNews(List.of(1L)));

        assertEquals("published", mockNews.getStatus());
    }

    @Test
    @DisplayName("审核退回")
    void shouldRejectNewsWithReason() {
        mockNews.setStatus("pending");
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        assertDoesNotThrow(() -> newsService.rejectNews(1L, "数据不足"));

        assertEquals("rejected", mockNews.getStatus());
        assertEquals("数据不足", mockNews.getRejectReason());
    }

    @Test
    @DisplayName("删除 — 作者可删自己的稿件")
    void shouldAllowAuthorToDelete() {
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        assertDoesNotThrow(() -> newsService.deleteNews(1L, 2L, "editor"));

        verify(newsMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除 — 管理员可删任何人的")
    void shouldAllowAdminToDeleteAnyNews() {
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        assertDoesNotThrow(() -> newsService.deleteNews(1L, 99L, "admin"));

        verify(newsMapper).deleteById(1L);
    }

    @Test
    @DisplayName("点击计数缓冲")
    void shouldBufferClickCounts() {
        newsService.incrementClick(1L);
        newsService.incrementClick(1L);
        newsService.incrementClick(2L);

        // 刷新前数据库不应被调用
        verify(newsMapper, never()).updateById(any());

        // 手动触发刷新
        when(newsMapper.selectById(1L)).thenReturn(mockNews);
        News other = new News(); other.setNewsId(2L); other.setClicked(5L);
        when(newsMapper.selectById(2L)).thenReturn(other);

        newsService.flushClickCounts();

        verify(newsMapper, atLeastOnce()).updateById(any());
    }
}
