package com.example.news.modules.recommend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.news.vo.NewsVO;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import com.example.news.modules.category.entity.Category;
import com.example.news.modules.category.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容推荐服务 — 基于 TF-IDF + 余弦相似度
 * 不依赖任何外部服务，纯 Java 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final NewsMapper newsMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    // 简单的缓存：文章ID -> 相似文章列表
    private final Map<Long, List<Long>> similarityCache = new LinkedHashMap<>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, List<Long>> eldest) {
            return size() > 100;
        }
    };

    /**
     * 获取与指定文章最相似的 N 篇文章（基于 TF-IDF 余弦相似度）
     */
    public List<NewsVO> getSimilarNews(Long newsId, int limit) {
        // 检查缓存
        List<Long> cached = similarityCache.get(newsId);
        if (cached != null) {
            return toVOList(cached.subList(0, Math.min(limit, cached.size())));
        }

        News source = newsMapper.selectById(newsId);
        if (source == null || source.getContent() == null) return fallbackByCategory(source, limit);

        // 获取所有已发布新闻
        List<News> allNews = newsMapper.selectList(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .ne(News::getNewsId, newsId));

        if (allNews.isEmpty()) return Collections.emptyList();

        // 分词 + TF-IDF 向量化
        Map<String, Double> sourceVector = tfidfVector(source, allNews);
        List<NewsSimilarity> rankings = new ArrayList<>();

        for (News n : allNews) {
            if (n.getContent() == null) continue;
            Map<String, Double> targetVector = tfidfVector(n, allNews);
            double similarity = cosineSimilarity(sourceVector, targetVector);
            rankings.add(new NewsSimilarity(n.getNewsId(), similarity));
        }

        rankings.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        List<Long> similarIds = rankings.stream()
                .limit(limit)
                .map(r -> r.newsId)
                .collect(Collectors.toList());

        similarityCache.put(newsId, similarIds);
        return toVOList(similarIds);
    }

    /**
     * 为用户推荐感兴趣的文章（基于其收藏/点赞历史）
     */
    public List<NewsVO> getRecommendedForUser(Long userId, int limit) {
        // 获取用户最近互动过的文章
        List<News> allPublished = newsMapper.selectList(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .orderByDesc(News::getClicked)
                .last("LIMIT 50"));

        if (allPublished.isEmpty()) return Collections.emptyList();

        // 简单策略：返回高点击的不同分类文章
        Set<Long> seenCategories = new HashSet<>();
        List<NewsVO> result = new ArrayList<>();

        for (News n : allPublished) {
            if (!seenCategories.contains(n.getCategoryId())) {
                seenCategories.add(n.getCategoryId());
                result.add(toVO(n));
                if (result.size() >= limit) break;
            }
        }

        return result;
    }

    // ========== TF-IDF 实现 ==========

    /**
     * 中文简单分词（Unigram + Bigram）
     */
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        text = text.replaceAll("[\\p{Punct}\\s]+", ""); // 去除标点和空白
        if (text.length() < 2) return tokens;

        // Unigram: 单字
        for (int i = 0; i < text.length(); i++) {
            tokens.add(String.valueOf(text.charAt(i)));
        }
        // Bigram: 双字词
        for (int i = 0; i < text.length() - 1; i++) {
            tokens.add(text.substring(i, i + 2));
        }
        return tokens;
    }

    /**
     * 计算文档的 TF-IDF 向量
     */
    private Map<String, Double> tfidfVector(News doc, List<News> corpus) {
        List<String> tokens = tokenize(doc.getContent());
        Map<String, Integer> termFreq = new HashMap<>();
        for (String t : tokens) {
            termFreq.merge(t, 1, Integer::sum);
        }

        int totalDocs = corpus.size();
        Map<String, Double> vector = new HashMap<>();

        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            double tf = (double) entry.getValue() / tokens.size();

            // IDF: 包含该词的文档数
            long docCount = corpus.stream()
                    .filter(n -> n.getContent() != null && n.getContent().contains(term))
                    .count();
            double idf = Math.log((double) (totalDocs + 1) / (docCount + 1)) + 1.0;

            vector.put(term, tf * idf);
        }
        return vector;
    }

    /**
     * 余弦相似度
     */
    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allTerms = new HashSet<>(v1.keySet());
        allTerms.addAll(v2.keySet());

        double dotProduct = 0, norm1 = 0, norm2 = 0;
        for (String term : allTerms) {
            double a = v1.getOrDefault(term, 0.0);
            double b = v2.getOrDefault(term, 0.0);
            dotProduct += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        if (norm1 == 0 || norm2 == 0) return 0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // ========== 降级策略 ==========
    private List<NewsVO> fallbackByCategory(News source, int limit) {
        if (source == null) return Collections.emptyList();
        List<News> sameCategory = newsMapper.selectList(new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .eq(News::getCategoryId, source.getCategoryId())
                .ne(News::getNewsId, source.getNewsId())
                .orderByDesc(News::getClicked)
                .last("LIMIT " + limit));
        return sameCategory.stream().map(this::toVO).collect(Collectors.toList());
    }

    // ========== VO 转换 ==========
    private List<NewsVO> toVOList(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        List<News> newsList = newsMapper.selectBatchIds(ids);
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        return newsList.stream().map(n -> {
            NewsVO vo = new NewsVO();
            vo.setNewsId(n.getNewsId());
            vo.setTitle(n.getTitle());
            vo.setClicked(n.getClicked());
            vo.setCreatedAt(n.getCreatedAt());
            vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
            vo.setCategoryId(n.getCategoryId());
            vo.setAuthorName(userMap.getOrDefault(n.getUserId(), "未知"));
            return vo;
        }).collect(Collectors.toList());
    }

    private NewsVO toVO(News n) {
        NewsVO vo = new NewsVO();
        vo.setNewsId(n.getNewsId());
        vo.setTitle(n.getTitle());
        vo.setClicked(n.getClicked());
        vo.setCreatedAt(n.getCreatedAt());
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
        vo.setCategoryName(catMap.getOrDefault(n.getCategoryId(), "未分类"));
        vo.setCategoryId(n.getCategoryId());
        return vo;
    }

    private record NewsSimilarity(Long newsId, double similarity) {}
}
