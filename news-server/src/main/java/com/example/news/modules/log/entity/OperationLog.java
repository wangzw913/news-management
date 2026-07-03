package com.example.news.modules.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("logs")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private String username;
    private String action;
    private String target;
    private String ip;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
