package com.example.news.modules.favorite.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorites")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long favId;
    private Long userId;
    private Long newsId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
