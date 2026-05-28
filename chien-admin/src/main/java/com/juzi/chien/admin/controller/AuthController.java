package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.RateLimit;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.domain.vo.LoginVO;
import com.juzi.chien.admin.domain.vo.MenuTreeVO;
import com.juzi.chien.admin.security.JwtUtils;
import com.juzi.chien.admin.security.LoginUser;
import com.juzi.chien.admin.service.OnlineUserService;
import com.juzi.chien.admin.service.SysLoginLogService;
import com.juzi.chien.admin.service.SysMenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "认证管理", description = "登录、登出、获取用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysMenuService menuService;
    private final OnlineUserService onlineUserService;
    private final SysLoginLogService loginLogService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT Token")
    @PostMapping("/login")
    @RateLimit(key = "login", count = 5, period = 60, dimension = "ip")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginVO loginVO, HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        String browser = parseBrowser(ua);
        String os = parseOs(ua);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginVO.getUsername(), loginVO.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername());

            // 存储到 Redis（在线用户管理）
            onlineUserService.login(loginUser.getUserId(), loginUser.getUsername(), token, ip);

            // 记录登录成功日志
            loginLogService.recordLogin(loginVO.getUsername(), ip, browser, os, 1, "登录成功");

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", loginUser.getUserId());
            data.put("username", loginUser.getUsername());

            return Result.success(data);
        } catch (Exception e) {
            // 记录登录失败日志
            loginLogService.recordLogin(loginVO.getUsername(), ip, browser, os, 0, "用户名或密码错误");
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 获取当前用户信息（含菜单树和权限列表）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUserId();

        // 查询菜单树（前端路由菜单，不含按钮）
        List<MenuTreeVO> menuTree = menuService.selectMenuTreeByUserId(userId);

        // 查询权限标识集合
        Set<String> permissions = menuService.selectPermsByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", loginUser.getUsername());
        data.put("permissions", permissions);
        data.put("menus", menuTree);

        return Result.success(data);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token != null) {
            onlineUserService.logout(token);
        }
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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

    private String parseBrowser(String ua) {
        if (ua == null) return "未知";
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Chrome/") && !ua.contains("Chromium")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("MSIE") || ua.contains("Trident/")) return "IE";
        return "其他";
    }

    private String parseOs(String ua) {
        if (ua == null) return "未知";
        if (ua.contains("Windows NT 10")) return "Windows 10/11";
        if (ua.contains("Windows NT 6.3")) return "Windows 8.1";
        if (ua.contains("Windows NT 6.1")) return "Windows 7";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "其他";
    }
}
