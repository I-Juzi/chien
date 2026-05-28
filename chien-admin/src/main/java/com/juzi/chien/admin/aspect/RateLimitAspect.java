package com.juzi.chien.admin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juzi.chien.admin.common.RateLimit;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 接口限流 AOP 切面
 * 基于 Redis + Lua 脚本实现滑动窗口限流
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Lua 脚本：原子性地判断并递增计数器
    private static final String LUA_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local expire = tonumber(ARGV[2]) " +
            "local current = tonumber(redis.call('GET', key) or '0') " +
            "if current + 1 > limit then " +
            "  return 0 " +
            "else " +
            "  redis.call('INCR', key) " +
            "  if current == 0 then " +
            "    redis.call('EXPIRE', key, expire) " +
            "  end " +
            "  return 1 " +
            "end";

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint joinPoint, RateLimit rateLimit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();
        String limitKey = buildKey(request, rateLimit);

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(redisScript,
                Collections.singletonList(limitKey),
                String.valueOf(rateLimit.count()),
                String.valueOf(rateLimit.period()));

        if (result != null && result == 0) {
            try {
                HttpServletResponse response = attributes.getResponse();
                if (response != null) {
                    response.setStatus(429);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(
                            Result.error(429, "请求过于频繁，请 " + rateLimit.period() + " 秒后再试")));
                }
            } catch (Exception e) {
                log.error("限流响应写入失败", e);
            }
            throw new RuntimeException("请求过于频繁");
        }
    }

    private String buildKey(HttpServletRequest request, RateLimit rateLimit) {
        StringBuilder sb = new StringBuilder("rate_limit:");
        if (rateLimit.key() != null && !rateLimit.key().isEmpty()) {
            sb.append(rateLimit.key()).append(":");
        } else {
            sb.append(request.getRequestURI()).append(":");
        }

        if ("user".equals(rateLimit.dimension())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
                sb.append("user:").append(loginUser.getUserId());
            } else {
                sb.append("ip:").append(getClientIp(request));
            }
        } else {
            sb.append("ip:").append(getClientIp(request));
        }

        return sb.toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
