package com.example.news.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 — 用于 AOP 自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {
    /** 操作描述 */
    String value() default "";
    /** 操作目标（支持 SpEL） */
    String target() default "";
}
