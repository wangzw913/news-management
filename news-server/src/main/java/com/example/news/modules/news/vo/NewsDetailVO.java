package com.example.news.modules.news.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsDetailVO {
    private Long newsId;
    private String title;
    private String summary;
    private String content;
    private String tags;
    private String source;
    private String coverImage;
    private String status;
    private String rejectReason;
    private Integer isTop;
    private Long clicked;
    private String authorName;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
