package com.example.news.modules.user;

import com.example.news.common.security.JwtTokenProvider;
import com.example.news.modules.user.controller.AuthController;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("认证 API 测试")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/auth/login — 登录成功返回 token")
    void loginSuccess() throws Exception {
        var dto = Map.of("username", "admin", "password", "admin123");
        User user = new User();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setRole("admin");
        user.setStatus(1);

        when(userService.login(any())).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(1L, "admin", "admin")).thenReturn("fake-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("fake-refresh");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("fake-token"))
                .andExpect(jsonPath("$.data.role").value("admin"))
                .andExpect(jsonPath("$.data.roleName").value("管理员"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — 密码错误")
    void loginFail() throws Exception {
        var dto = Map.of("username", "admin", "password", "wrong");

        when(userService.login(any()))
                .thenThrow(new com.example.news.common.exception.BusinessException(1003, "用户名或密码错误"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — 注册成功")
    void registerSuccess() throws Exception {
        var dto = Map.of("username", "newuser", "password", "123456", "email", "test@qq.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
