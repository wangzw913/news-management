package com.example.news.modules.slide.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("slides")
public class Slide {
    @TableId(type = IdType.AUTO)
    private Long slideId;
    private String title;
    private String image;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
