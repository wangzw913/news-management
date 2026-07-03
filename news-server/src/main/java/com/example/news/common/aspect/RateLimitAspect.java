package com.example.news.common.aspect;

import com.example.news.common.annotation.RateLimit;
import com.example.news.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 限流 AOP 切面 — 基于 Redis 滑动窗口计数器
 */
@Slf4j
@Aspect
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(rateLimit);
        long window = rateLimit.timeUnit().toSeconds(rateLimit.window());

        // Redis INCR + EXPIRE 实现计数窗口
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, window, TimeUnit.SECONDS);
        }

        if (count != null && count > rateLimit.limit()) {
            log.warn("限流触发: key={}, count={}, limit={}", key, count, rateLimit.limit());
            throw new BusinessException(6001, rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private String buildKey(RateLimit rl) {
        String userId = "anon";
        try {
            userId = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception ignored) {}
        String uri = "";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                uri = request.getRequestURI();
            }
        } catch (Exception ignored) {}
        return "rate_limit:" + rl.key() + ":" + userId + ":" + uri;
    }
}
