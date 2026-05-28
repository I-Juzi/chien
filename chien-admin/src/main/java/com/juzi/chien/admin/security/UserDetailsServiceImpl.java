package com.juzi.chien.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juzi.chien.admin.domain.entity.SysMenu;
import com.juzi.chien.admin.domain.entity.SysRole;
import com.juzi.chien.admin.domain.entity.SysUser;
import com.juzi.chien.admin.mapper.SysMenuMapper;
import com.juzi.chien.admin.mapper.SysRoleMapper;
import com.juzi.chien.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetailsService 实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, 1)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());

        // 查询用户权限（从菜单表中的按钮类型获取权限标识）
        List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = menus.stream()
                .filter(menu -> "F".equals(menu.getMenuType()) && menu.getPerms() != null && !menu.getPerms().isEmpty())
                .map(menu -> new SimpleGrantedAuthority(menu.getPerms()))
                .collect(Collectors.toList());

        // 将角色也作为权限加入
        roles.stream()
                .filter(role -> role.getRoleKey() != null)
                .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleKey())));

        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }
}
