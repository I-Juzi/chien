package com.juzi.chien.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单权限表实体类
 */
@Data
@TableName("`chien-menu`")
public class SysMenu implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID（0=顶级） */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 是否显示（0=隐藏 1=显示） */
    private Integer isVisible;

    /** 菜单类型（M=目录 C=菜单 F=按钮） */
    private String menuType;

    /** 权限标识（如 system:user:query） */
    private String perms;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
