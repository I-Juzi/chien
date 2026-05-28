package com.juzi.chien.admin.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线用户 VO
 */
@Data
public class OnlineUserVO implements Serializable {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** Token（用于强退） */
    private String token;

    /** 登录IP */
    private String ip;

    /** 登录时间 */
    private String loginTime;

    /** 最后活跃时间 */
    private String lastActiveTime;
}
