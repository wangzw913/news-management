package com.example.news.modules.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("news")
public class News {
    @TableId(type = IdType.AUTO)
    private Long newsId;
    private Long userId;
    private Long categoryId;
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
    private Integer isEsSynced;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
