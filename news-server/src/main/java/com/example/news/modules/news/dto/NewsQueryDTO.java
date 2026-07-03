package com.example.news.modules.news.dto;

import lombok.Data;

@Data
public class NewsQueryDTO {
    private int page = 1;
    private int size = 10;
    private String status;
    private Long categoryId;
    private String keyword;
    private Boolean isTop;
}
