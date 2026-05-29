package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysRole;

import java.util.List;

/**
 * 角色 Service 接口
 */
public interface SysRoleService {

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 查询所有角色
     */
    List<SysRole> selectAllRoles();

    /**
     * 根据ID查询角色
     */
    SysRole selectRoleById(Long roleId);

    /**
     * 新增角色
     */
    int insertRole(SysRole role);

    /**
     * 修改角色
     */
    int updateRole(SysRole role);

    /**
     * 删除角色（逻辑删除）
     */
    int deleteRoleById(Long roleId);

    /**
     * 更新角色菜单关联
     */
    void updateRoleMenu(Long roleId, List<Long> menuIds);
}
