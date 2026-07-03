package com.example.news.modules.history.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("read_history")
public class ReadHistory {
    @TableId(type = IdType.AUTO)
    private Long histId;
    private Long userId;
    private Long newsId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
