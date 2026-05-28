package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.juzi.chien.admin.domain.vo.OnlineUserVO;
import com.juzi.chien.admin.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户 Controller
 */
@Tag(name = "在线用户", description = "在线用户查询、强退")
@RestController
@RequestMapping("/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * 获取在线用户列表
     */
    @GetMapping("/list")
    public Result<List<OnlineUserVO>> list() {
        List<OnlineUserVO> list = onlineUserService.listOnlineUsers();
        return Result.success(list);
    }

    /**
     * 强退用户（按 token）
     */
    @DeleteMapping("/kick/{token}")
    public Result<Void> kickByToken(@PathVariable String token) {
        onlineUserService.kickByToken(token);
        return Result.success();
    }

    /**
     * 强退用户（按 userId，踢掉所有会话）
     */
    @DeleteMapping("/kick/user/{userId}")
    public Result<Void> kickByUserId(@PathVariable Long userId) {
        onlineUserService.kickByUserId(userId);
        return Result.success();
    }
}
