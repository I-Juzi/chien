package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.juzi.chien.admin.domain.entity.SysLoginLog;
import com.juzi.chien.admin.mapper.SysLoginLogMapper;
import com.juzi.chien.admin.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    @Async
    public void recordLogin(String username, String ip, String browser, String os, int status, String msg) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setBrowser(browser);
        log.setOs(os);
        log.setStatus(status);
        log.setMsg(msg);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    @Override
    public IPage<SysLoginLog> selectPage(int pageNum, int pageSize, String username, Integer status) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLoginLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return loginLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void deleteById(Long id) {
        loginLogMapper.deleteById(id);
    }

    @Override
    public void clean() {
        loginLogMapper.delete(null);
    }
}
