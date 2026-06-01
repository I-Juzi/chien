package com.juzi.chien.admin.service.impl;

import com.juzi.chien.admin.domain.entity.SysMenu;
import com.juzi.chien.admin.domain.vo.MenuTreeVO;
import com.juzi.chien.admin.mapper.SysMenuMapper;
import com.juzi.chien.admin.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单权限 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> selectMenusByUserId(Long userId) {
        return menuMapper.selectMenusByUserId(userId);
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return menus.stream()
                .filter(menu -> "F".equals(menu.getMenuType())
                        && menu.getPerms() != null
                        && !menu.getPerms().isEmpty())
                .map(SysMenu::getPerms)
                .collect(Collectors.toSet());
    }

    @Override
    public List<MenuTreeVO> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);

        // 只保留目录和菜单（排除按钮），隐藏菜单也返回（前端通过 meta.hidden 控制侧边栏显示）
        List<SysMenu> visibleMenus = menus.stream()
                .filter(menu -> !"F".equals(menu.getMenuType()))
                .collect(Collectors.toList());

        return buildMenuTree(visibleMenus, 0L);
    }

    /**
     * 递归构建菜单树
     */
    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> Objects.equals(menu.getParentId(), parentId))
                .map(menu -> {
                    MenuTreeVO vo = new MenuTreeVO();
                    vo.setId(menu.getId());
                    vo.setParentId(menu.getParentId());
                    vo.setMenuName(menu.getMenuName());
                    vo.setPath(menu.getPath());
                    vo.setComponent(menu.getComponent());
                    vo.setIcon(menu.getIcon());
                    vo.setSortOrder(menu.getSortOrder());
                    vo.setIsVisible(menu.getIsVisible());
                    vo.setMenuType(menu.getMenuType());
                    vo.setPerms(menu.getPerms());
                    vo.setChildren(buildMenuTree(menus, menu.getId()));
                    return vo;
                })
                .sorted(Comparator.comparing(MenuTreeVO::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> selectAllMenus() {
        return menuMapper.selectList(null);
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        return menuMapper.selectById(menuId);
    }

    @Override
    public int insertMenu(SysMenu menu) {
        return menuMapper.insert(menu);
    }

    @Override
    public int updateMenu(SysMenu menu) {
        return menuMapper.updateById(menu);
    }

    @Override
    public int deleteMenuById(Long menuId) {
        return menuMapper.deleteById(menuId);
    }
}
