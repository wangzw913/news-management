package com.example.news.modules.user;

import com.example.news.common.exception.BusinessException;
import com.example.news.modules.user.dto.LoginDTO;
import com.example.news.modules.user.dto.RegisterDTO;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import com.example.news.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    private UserService userService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(passwordEncoder);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);

        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("$2a$10$encodedPassword");
        mockUser.setRole("user");
        mockUser.setStatus(1);
    }

    @Test
    @DisplayName("登录成功")
    void shouldLoginSuccessfully() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        // MyBatis-Plus calls selectOne(wrapper, true) with 2 args
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", mockUser.getPassword())).thenReturn(true);

        User result = userService.login(dto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("user", result.getRole());
    }

    @Test
    @DisplayName("登录失败 — 密码错误")
    void shouldFailOnWrongPassword() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("wrong");

        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", mockUser.getPassword())).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录失败 — 用户不存在")
    void shouldFailOnNonexistentUser() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nobody");
        dto.setPassword("123456");

        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录失败 — 账号已停用")
    void shouldFailOnDisabledAccount() {
        mockUser.setStatus(0);
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", mockUser.getPassword())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("注册成功")
    void shouldRegisterSuccessfully() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encoded");

        assertDoesNotThrow(() -> userService.register(dto));
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 — 用户名已存在")
    void shouldFailOnDuplicateUsername() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existing");
        dto.setPassword("123456");

        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> userService.register(dto));
        verify(userMapper, never()).insert(any());
    }
}
