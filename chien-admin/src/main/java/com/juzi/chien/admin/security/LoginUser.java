package com.juzi.chien.admin.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 登录用户详情，扩展 Spring Security 的 User
 */
@Getter
public class LoginUser extends User {

    /** 用户ID */
    private final Long userId;

    public LoginUser(Long userId, String username, String password,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
}
