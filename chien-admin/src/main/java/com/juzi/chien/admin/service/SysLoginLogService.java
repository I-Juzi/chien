package com.juzi.chien.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.juzi.chien.admin.domain.entity.SysLoginLog;

public interface SysLoginLogService {
    void recordLogin(String username, String ip, String browser, String os, int status, String msg);
    IPage<SysLoginLog> selectPage(int pageNum, int pageSize, String username, Integer status);
    void deleteById(Long id);
    void clean();
}
