package com.juzi.chien.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juzi.chien.admin.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联表 Mapper 接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
