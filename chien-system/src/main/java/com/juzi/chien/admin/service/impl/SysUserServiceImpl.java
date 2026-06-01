package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juzi.chien.admin.domain.entity.SysUser;
import com.juzi.chien.admin.domain.entity.SysUserRole;
import com.juzi.chien.admin.mapper.SysUserMapper;
import com.juzi.chien.admin.mapper.SysUserRoleMapper;
import com.juzi.chien.admin.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SysUser selectUserByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
    }

    @Override
    public List<SysUser> selectAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public SysUser selectUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public int insertUser(SysUser user) {
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user);
    }

    @Override
    public int updateUser(SysUser user) {
        // 不允许通过此接口修改密码
        user.setPassword(null);
        return userMapper.updateById(user);
    }

    @Override
    public int deleteUserById(Long userId) {
        return userMapper.deleteById(userId);
    }

    @Override
    public int resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        // 先删除原有用户角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        // 再批量插入新的关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = roleIds.stream()
                    .map(roleId -> {
                        SysUserRole ur = new SysUserRole();
                        ur.setUserId(userId);
                        ur.setRoleId(roleId);
                        return ur;
                    })
                    .collect(Collectors.toList());
            for (SysUserRole ur : userRoles) {
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        // 更新新密码
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
        return true;
    }
}
