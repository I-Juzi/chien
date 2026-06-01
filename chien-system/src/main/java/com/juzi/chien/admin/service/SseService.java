package com.juzi.chien.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 服务 —— 管理客户端连接和消息推送
 */
@Slf4j
@Service
public class SseService {

    /** 存储所有在线用户的 SSE 连接，key = userId */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 创建 SSE 连接
     */
    public SseEmitter subscribe(Long userId) {
        // 设置超时时间（0 = 不超时）
        SseEmitter emitter = new SseEmitter(0L);

        // 连接关闭/超时/异常时自动移除
        emitter.onCompletion(() -> remove(userId));
        emitter.onTimeout(() -> remove(userId));
        emitter.onError(e -> remove(userId));

        // 如果该用户已有旧连接，先移除
        SseEmitter old = emitters.get(userId);
        if (old != null) {
            try { old.complete(); } catch (Exception ignored) {}
        }

        emitters.put(userId, emitter);
        log.info("SSE 连接建立: userId={}, 当前在线: {}", userId, emitters.size());

        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("连接成功"));
        } catch (IOException e) {
            remove(userId);
        }

        return emitter;
    }

    /**
     * 向指定用户推送消息
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.warn("SSE 推送失败: userId={}", userId);
            remove(userId);
        }
    }

    /**
     * 向所有在线用户广播消息
     */
    public void broadcast(String eventName, Object data) {
        log.info("SSE 广播: event={}, 当前在线: {}", eventName, emitters.size());
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("SSE 广播失败: userId={}", userId);
                remove(userId);
            }
        });
    }

    /**
     * 移除连接
     */
    public void remove(Long userId) {
        emitters.remove(userId);
        log.info("SSE 连接断开: userId={}, 当前在线: {}", userId, emitters.size());
    }

    /**
     * 获取当前在线连接数
     */
    public int getOnlineCount() {
        return emitters.size();
    }
}
