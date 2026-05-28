package com.juzi.chien.admin.common;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于 Redis 实现，按 IP 或用户维度限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流 key 前缀 */
    String key() default "";

    /** 时间窗口内最大请求数 */
    int count() default 10;

    /** 时间窗口（秒） */
    int period() default 60;

    /** 限流维度：ip=按IP限流，user=按用户限流 */
    String dimension() default "ip";
}
