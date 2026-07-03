package com.example.news.modules.like.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("likes")
public class Like {
    @TableId(type = IdType.AUTO)
    private Long likeId;
    private Long userId;
    private Long newsId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
