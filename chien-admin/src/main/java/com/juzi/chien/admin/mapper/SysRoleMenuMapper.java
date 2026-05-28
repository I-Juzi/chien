package com.juzi.chien.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juzi.chien.admin.domain.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联表 Mapper 接口
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
