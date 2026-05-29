package com.juzi.chien.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色-菜单关联表实体类
 */
@Data
@TableName("`chien-role_menu`")
public class SysRoleMenu implements Serializable {

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;
}
