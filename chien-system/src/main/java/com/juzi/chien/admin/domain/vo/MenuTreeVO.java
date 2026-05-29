package com.juzi.chien.admin.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单树形结构 VO
 */
@Data
public class MenuTreeVO implements Serializable {

    private Long id;

    /** 父菜单ID */
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

    /** 权限标识 */
    private String perms;

    /** 子菜单 */
    private List<MenuTreeVO> children;
}
