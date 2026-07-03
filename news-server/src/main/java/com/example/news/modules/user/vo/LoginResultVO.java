package com.example.news.modules.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVO {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String role;
    private String roleName;
}
