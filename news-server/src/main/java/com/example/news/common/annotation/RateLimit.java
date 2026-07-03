package com.example.news.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解 — 基于 Redis 滑动窗口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /** 限流 key */
    String key() default "";
    /** 时间窗口内允许的次数 */
    int limit() default 10;
    /** 时间窗口 */
    int window() default 60;
    /** 时间单位 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    /** 限流提示 */
    String message() default "操作过于频繁，请稍后重试";
}
