package com.example.news.modules.user.service;

import com.example.news.common.utils.RedisUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token 管理 — 存 Redis
 */
@Service
@ConditionalOnBean(RedisUtil.class)
public class RefreshTokenService {

    private final RedisUtil redisUtil;

    public RefreshTokenService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    private static final String PREFIX = "refresh_token:";
    private static final long TTL_DAYS = 7;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * 生成并存储 Refresh Token
     */
    public String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisUtil.setString(PREFIX + token, String.valueOf(userId), TTL_DAYS, TimeUnit.DAYS);
        return token;
    }

    /**
     * 验证并返回 userId
     */
    public Long validateRefreshToken(String token) {
        String userIdStr = redisUtil.getString(PREFIX + token);
        if (userIdStr == null) return null;
        return Long.parseLong(userIdStr);
    }

    /**
     * 删除 Refresh Token（登出时）
     */
    public void revokeRefreshToken(String token) {
        redisUtil.delete(PREFIX + token);
    }

    /**
     * 将 Access Token 加入黑名单
     */
    public void blacklistAccessToken(String accessToken, long ttlSeconds) {
        redisUtil.setString(BLACKLIST_PREFIX + accessToken, "1", ttlSeconds, TimeUnit.SECONDS);
    }
}
