package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysMenu;
import com.juzi.chien.admin.domain.vo.MenuTreeVO;

import java.util.List;
import java.util.Set;

/**
 * 菜单权限 Service 接口
 */
public interface SysMenuService {

    /**
     * 根据用户ID查询菜单列表
     */
    List<SysMenu> selectMenusByUserId(Long userId);

    /**
     * 根据用户ID查询权限标识集合
     */
    Set<String> selectPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单树（仅目录和菜单，不含按钮）
     */
    List<MenuTreeVO> selectMenuTreeByUserId(Long userId);

    /**
     * 查询所有菜单列表
     */
    List<SysMenu> selectAllMenus();

    /**
     * 根据ID查询菜单
     */
    SysMenu selectMenuById(Long menuId);

    /**
     * 新增菜单
     */
    int insertMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    int updateMenu(SysMenu menu);

    /**
     * 删除菜单（逻辑删除）
     */
    int deleteMenuById(Long menuId);
}
