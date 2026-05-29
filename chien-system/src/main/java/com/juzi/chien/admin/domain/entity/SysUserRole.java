package com.juzi.chien.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户-角色关联表实体类
 */
@Data
@TableName("`chien-user_role`")
public class SysUserRole implements Serializable {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}
