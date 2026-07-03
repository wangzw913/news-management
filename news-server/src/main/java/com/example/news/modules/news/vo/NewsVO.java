package com.example.news.modules.news.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsVO {
    private Long newsId;
    private String title;
    private String summary;
    private String coverImage;
    private String status;
    private String rejectReason;
    private Integer isTop;
    private Long clicked;
    private String content;
    private String authorName;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
