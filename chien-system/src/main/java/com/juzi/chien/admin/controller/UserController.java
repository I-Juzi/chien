package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.juzi.chien.admin.domain.entity.SysUser;
import com.juzi.chien.admin.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户的增删改查、重置密码、分配角色")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<SysUser>> list() {
        List<SysUser> users = userService.selectAllUsers();
        // 脱敏：清除密码字段
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<SysUser> getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysUser user) {
        userService.insertUser(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户（支持批量，逗号分隔）
     */
    @DeleteMapping("/{userIds}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] userIds) {
        for (Long userId : userIds) {
            userService.deleteUserById(userId);
        }
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/resetPwd")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> resetPwd(@RequestBody SysUser user) {
        userService.resetPassword(user.getId(), user.getPassword());
        return Result.success();
    }

    /**
     * 更新用户角色关联
     */
    @PutMapping("/changeRole")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> changeRole(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        userService.updateUserRoles(userId, roleIds);
        return Result.success();
    }
}
