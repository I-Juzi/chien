package com.juzi.chien.admin.service.impl;

import com.juzi.chien.admin.domain.entity.SysRole;
import com.juzi.chien.admin.domain.entity.SysRoleMenu;
import com.juzi.chien.admin.mapper.SysRoleMapper;
import com.juzi.chien.admin.mapper.SysRoleMenuMapper;
import com.juzi.chien.admin.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public List<SysRole> selectAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public SysRole selectRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public int insertRole(SysRole role) {
        return roleMapper.insert(role);
    }

    @Override
    public int updateRole(SysRole role) {
        return roleMapper.updateById(role);
    }

    @Override
    public int deleteRoleById(Long roleId) {
        return roleMapper.deleteById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleMenu(Long roleId, List<Long> menuIds) {
        // 先删除原有角色菜单关联
        roleMenuMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
        // 再批量插入新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> roleMenus = menuIds.stream()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .collect(Collectors.toList());
            for (SysRoleMenu rm : roleMenus) {
                roleMenuMapper.insert(rm);
            }
        }
    }
}
