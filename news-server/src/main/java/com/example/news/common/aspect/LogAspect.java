package com.example.news.common.aspect;

import com.example.news.common.annotation.LogOperation;
import com.example.news.common.utils.IpUtil;
import com.example.news.common.utils.ServletUtils;
import com.example.news.modules.log.entity.OperationLog;
import com.example.news.modules.log.mapper.LogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LogMapper logMapper;

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        Object result = joinPoint.proceed();
        // 异步记录日志
        saveLogAsync(logOperation);
        return result;
    }

    @Async
    public void saveLogAsync(LogOperation logOp) {
        try {
            OperationLog opLog = new OperationLog();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                opLog.setUsername(auth.getName());
            } else {
                opLog.setUsername("系统");
            }
            opLog.setAction(logOp.value());
            opLog.setTarget(logOp.target());
            opLog.setCreatedAt(LocalDateTime.now());

            ServletUtils.getCurrentRequest().ifPresent(request -> {
                opLog.setIp(IpUtil.getClientIp(request));
            });

            logMapper.insert(opLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
}
