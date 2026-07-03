package com.example.news.modules.user.controller;

import com.example.news.common.annotation.LogOperation;
import com.example.news.common.annotation.RateLimit;
import com.example.news.common.exception.BusinessException;
import com.example.news.common.exception.ErrorCode;
import com.example.news.common.response.Result;
import com.example.news.common.security.JwtTokenProvider;
import com.example.news.modules.user.dto.LoginDTO;
import com.example.news.modules.user.dto.RegisterDTO;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.service.RefreshTokenService;
import com.example.news.modules.user.service.UserService;
import com.example.news.modules.user.vo.LoginResultVO;
import com.example.news.modules.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证", description = "登录、注册、Token 刷新、登出")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired(required = false)
    private RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "登录")
    @RateLimit(key = "login", limit = 10, window = 60)
    @LogOperation(value = "登录系统")
    @PostMapping("/login")
    public Result<LoginResultVO> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.login(dto);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());
        String refreshToken = refreshTokenService != null ?
                refreshTokenService.createRefreshToken(user.getUserId()) :
                jwtTokenProvider.generateRefreshToken(user.getUserId());
        return Result.ok(userService.toLoginResult(user, accessToken, refreshToken));
    }

    @Operation(summary = "注册")
    @RateLimit(key = "register", limit = 5, window = 60)
    @LogOperation(value = "新用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @Operation(summary = "刷新 Access Token")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        // 如果 Redis 不可用，用 JWT refresh token
        Long userId;
        if (refreshTokenService != null) {
            userId = refreshTokenService.validateRefreshToken(refreshToken);
            if (userId != null) refreshTokenService.revokeRefreshToken(refreshToken);
        } else {
            // JWT refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }
            userId = jwtTokenProvider.getUserId(refreshToken);
        }
        if (userId == null) throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        User user = userService.getById(userId);
        if (user == null || user.getStatus() == 0) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());
        String newRefreshToken = refreshTokenService != null ?
                refreshTokenService.createRefreshToken(user.getUserId()) :
                jwtTokenProvider.generateRefreshToken(user.getUserId());
        return Result.ok(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                               @RequestBody(required = false) Map<String, String> body) {
        if (refreshTokenService != null) {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                long remaining = jwtTokenProvider.getAccessTokenExpiration() / 1000;
                refreshTokenService.blacklistAccessToken(accessToken, remaining);
            }
            if (body != null && body.containsKey("refreshToken")) {
                refreshTokenService.revokeRefreshToken(body.get("refreshToken"));
            }
        }
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        Long userId = jwtTokenProvider.getUserId(token);
        User user = userService.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        return Result.ok(userService.toVO(user));
    }
}
