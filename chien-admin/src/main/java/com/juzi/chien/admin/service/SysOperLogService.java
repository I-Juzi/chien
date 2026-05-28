package com.juzi.chien.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.juzi.chien.admin.domain.entity.SysOperLog;

/**
 * 操作日志 Service
 */
public interface SysOperLogService {

    /**
     * 新增操作日志
     */
    void insertLog(SysOperLog operLog);

    /**
     * 分页查询操作日志
     */
    IPage<SysOperLog> selectLogPage(int pageNum, int pageSize, String title, Integer businessType, Integer status);

    /**
     * 根据ID查询
     */
    SysOperLog selectLogById(Long id);

    /**
     * 删除日志
     */
    void deleteLogById(Long id);

    /**
     * 清空日志
     */
    void cleanLog();
}
