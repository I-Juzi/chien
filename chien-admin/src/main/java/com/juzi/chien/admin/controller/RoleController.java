package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.juzi.chien.admin.domain.entity.SysRole;
import com.juzi.chien.admin.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理", description = "角色的增删改查、分配菜单权限")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService roleService;

    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.selectAllRoles());
    }

    /**
     * 根据ID查询角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<SysRole> getInfo(@PathVariable Long roleId) {
        return Result.success(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysRole role) {
        roleService.insertRole(role);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysRole role) {
        roleService.updateRole(role);
        return Result.success();
    }

    /**
     * 删除角色（支持批量，逗号分隔）
     */
    @DeleteMapping("/{roleIds}")
    @PreAuthorize("hasAuthority('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] roleIds) {
        for (Long roleId : roleIds) {
            roleService.deleteRoleById(roleId);
        }
        return Result.success();
    }

    /**
     * 更新角色菜单关联（授权菜单权限）
     */
    @PutMapping("/changeMenu")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> changeMenu(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        roleService.updateRoleMenu(roleId, menuIds);
        return Result.success();
    }
}
