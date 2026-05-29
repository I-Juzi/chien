package com.juzi.chien.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juzi.chien.admin.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据角色ID查询用户列表
     */
    List<SysUser> selectUsersByRoleId(@Param("roleId") Long roleId);
}
