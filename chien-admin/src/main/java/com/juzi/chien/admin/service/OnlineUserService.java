package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.vo.OnlineUserVO;

import java.util.List;

/**
 * 在线用户 Service
 */
public interface OnlineUserService {

    /**
     * 用户上线：存储会话到 Redis
     */
    void login(Long userId, String username, String token, String ip);

    /**
     * 用户下线：从 Redis 移除会话
     */
    void logout(String token);

    /**
     * 刷新活跃时间（每次认证请求时调用）
     */
    void refreshActiveTime(String token);

    /**
     * 检查 token 是否在线
     */
    boolean isOnline(String token);

    /**
     * 查询所有在线用户
     */
    List<OnlineUserVO> listOnlineUsers();

    /**
     * 强退用户（按 token）
     */
    void kickByToken(String token);

    /**
     * 强退用户（按 userId，踢掉该用户所有会话）
     */
    void kickByUserId(Long userId);
}
