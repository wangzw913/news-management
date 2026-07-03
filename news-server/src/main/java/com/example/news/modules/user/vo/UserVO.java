package com.example.news.modules.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String roleName;
    private String avatar;
    private Integer status;
    private LocalDateTime createdAt;
}
