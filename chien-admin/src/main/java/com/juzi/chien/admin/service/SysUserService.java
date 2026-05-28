package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysUser;

import java.util.List;

/**
 * 用户 Service 接口
 */
public interface SysUserService {

    /**
     * 根据用户名查询用户
     */
    SysUser selectUserByUsername(String username);

    /**
     * 查询所有用户
     */
    List<SysUser> selectAllUsers();

    /**
     * 根据ID查询用户
     */
    SysUser selectUserById(Long userId);

    /**
     * 新增用户（密码自动加密）
     */
    int insertUser(SysUser user);

    /**
     * 修改用户
     */
    int updateUser(SysUser user);

    /**
     * 删除用户（逻辑删除）
     */
    int deleteUserById(Long userId);

    /**
     * 重置用户密码
     */
    int resetPassword(Long userId, String newPassword);

    /**
     * 更新用户角色关联
     */
    void updateUserRoles(Long userId, List<Long> roleIds);
}
