package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.service.SseService;
import com.juzi.chien.admin.service.SysUserService;
import com.juzi.chien.admin.domain.entity.SysUser;
import com.juzi.chien.admin.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE", description = "服务端推送通知")
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;

    /**
     * 建立 SSE 连接
     * EventSource 不支持自定义 Header，token 通过 URL 参数传递
     */
    @Operation(summary = "订阅通知推送")
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String token) {
        // 手动验证 token
        Long userId = jwtUtils.getUserIdFromToken(token);
        SysUser user = userService.selectUserById(userId);
        if (user == null || user.getStatus() != 1) {
            return null;
        }
        return sseService.subscribe(userId);
    }
}
