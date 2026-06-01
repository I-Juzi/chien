package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysFileRecord;

import java.util.List;

public interface SysFileRecordService {

    /**
     * 保存文件记录
     */
    void insert(SysFileRecord record);

    /**
     * 查询所有文件记录
     */
    List<SysFileRecord> selectAll();

    /**
     * 根据ID查询
     */
    SysFileRecord selectById(Long id);

    /**
     * 删除文件记录（逻辑删除）
     */
    int deleteById(Long id);
}
