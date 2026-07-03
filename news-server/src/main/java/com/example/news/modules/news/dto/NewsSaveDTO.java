package com.example.news.modules.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewsSaveDTO {
    private Long newsId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;
    private String content;
    private String tags;
    private String source;

    @NotNull(message = "分类不能为空")
    private Long categoryId;
    private String coverImage;
}
