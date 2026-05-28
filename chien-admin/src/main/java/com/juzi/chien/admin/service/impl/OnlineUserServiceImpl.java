package com.juzi.chien.admin.service.impl;

import com.juzi.chien.admin.domain.vo.OnlineUserVO;
import com.juzi.chien.admin.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线用户 Service 实现（基于 Redis）
 *
 * Redis 数据结构：
 *   Key: online:token:{token}   →  Hash: userId, username, ip, loginTime, lastActiveTime
 *   Key: online:user:{userId}   →  Set:  存储该用户所有 token（支持多端登录）
 *   TTL: 30 分钟（每次请求刷新）
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "online:token:";
    private static final String USER_PREFIX = "online:user:";
    private static final long EXPIRE_MINUTES = 30;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void login(Long userId, String username, String token, String ip) {
        String now = LocalDateTime.now().format(FMT);
        String tokenKey = TOKEN_PREFIX + token;
        String userKey = USER_PREFIX + userId;

        // 存储会话信息
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("username", username);
        map.put("token", token);
        map.put("ip", ip != null ? ip : "未知");
        map.put("loginTime", now);
        map.put("lastActiveTime", now);

        redisTemplate.opsForHash().putAll(tokenKey, map);
        redisTemplate.expire(tokenKey, java.time.Duration.ofMinutes(EXPIRE_MINUTES));

        // 记录用户的 token 集合
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, java.time.Duration.ofMinutes(EXPIRE_MINUTES));
    }

    @Override
    public void logout(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(tokenKey);
        if (!entries.isEmpty()) {
            String userId = (String) entries.get("userId");
            redisTemplate.delete(tokenKey);
            // 从用户 token 集合中移除
            String userKey = USER_PREFIX + userId;
            redisTemplate.opsForSet().remove(userKey, token);
            // 如果用户没有其他会话，删除用户 key
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().size(userKey) == 0L)) {
                redisTemplate.delete(userKey);
            }
        }
    }

    @Override
    public void refreshActiveTime(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
            redisTemplate.opsForHash().put(tokenKey, "lastActiveTime", LocalDateTime.now().format(FMT));
            redisTemplate.expire(tokenKey, java.time.Duration.ofMinutes(EXPIRE_MINUTES));
            // 也刷新用户的 key
            String userId = (String) redisTemplate.opsForHash().get(tokenKey, "userId");
            if (userId != null) {
                redisTemplate.expire(USER_PREFIX + userId, java.time.Duration.ofMinutes(EXPIRE_MINUTES));
            }
        }
    }

    @Override
    public boolean isOnline(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }

    @Override
    public List<OnlineUserVO> listOnlineUsers() {
        Set<String> keys = redisTemplate.keys(TOKEN_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return keys.stream()
                .map(key -> {
                    Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                    if (map.isEmpty()) return null;
                    OnlineUserVO vo = new OnlineUserVO();
                    vo.setId(Long.parseLong((String) map.get("userId")));
                    vo.setUserId(Long.parseLong((String) map.get("userId")));
                    vo.setUsername((String) map.get("username"));
                    vo.setToken((String) map.get("token"));
                    vo.setIp((String) map.get("ip"));
                    vo.setLoginTime((String) map.get("loginTime"));
                    vo.setLastActiveTime((String) map.get("lastActiveTime"));
                    return vo;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(OnlineUserVO::getLoginTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void kickByToken(String token) {
        logout(token);
    }

    @Override
    public void kickByUserId(Long userId) {
        String userKey = USER_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);
        if (tokens != null) {
            tokens.forEach(token -> redisTemplate.delete(TOKEN_PREFIX + token));
        }
        redisTemplate.delete(userKey);
    }
}
