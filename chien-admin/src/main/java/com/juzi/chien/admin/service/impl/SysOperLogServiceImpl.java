package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.juzi.chien.admin.domain.entity.SysOperLog;
import com.juzi.chien.admin.mapper.SysOperLogMapper;
import com.juzi.chien.admin.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements SysOperLogService {

    private final SysOperLogMapper operLogMapper;

    @Override
    @Async
    public void insertLog(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }

    @Override
    public IPage<SysOperLog> selectLogPage(int pageNum, int pageSize, String title, Integer businessType, Integer status) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(SysOperLog::getTitle, title);
        }
        if (businessType != null) {
            wrapper.eq(SysOperLog::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(SysOperLog::getStatus, status);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return operLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public SysOperLog selectLogById(Long id) {
        return operLogMapper.selectById(id);
    }

    @Override
    public void deleteLogById(Long id) {
        operLogMapper.deleteById(id);
    }

    @Override
    public void cleanLog() {
        operLogMapper.delete(null);
    }
}
