package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.juzi.chien.admin.domain.entity.SysMenu;
import com.juzi.chien.admin.domain.vo.MenuTreeVO;
import com.juzi.chien.admin.security.LoginUser;
import com.juzi.chien.admin.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理", description = "菜单的增删改查、获取菜单树")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuService menuService;

    /**
     * 查询菜单列表（全部）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.selectAllMenus());
    }

    /**
     * 根据ID查询菜单详情
     */
    @GetMapping("/{menuId}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<SysMenu> getInfo(@PathVariable Long menuId) {
        return Result.success(menuService.selectMenuById(menuId));
    }

    /**
     * 获取当前用户的菜单树（用于前端路由菜单）
     */
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(menuService.selectMenuTreeByUserId(loginUser.getUserId()));
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysMenu menu) {
        menuService.insertMenu(menu);
        return Result.success();
    }

    /**
     * 修改菜单
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return Result.success();
    }

    /**
     * 删除菜单（支持批量，逗号分隔）
     */
    @DeleteMapping("/{menuIds}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] menuIds) {
        for (Long menuId : menuIds) {
            menuService.deleteMenuById(menuId);
        }
        return Result.success();
    }
}
