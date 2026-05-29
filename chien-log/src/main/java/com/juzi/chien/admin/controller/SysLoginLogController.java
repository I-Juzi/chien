package com.juzi.chien.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.domain.entity.SysLoginLog;
import com.juzi.chien.admin.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录日志", description = "登录日志查询、删除、清空")
@RestController
@RequestMapping("/loginlog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService loginLogService;

    @GetMapping("/list")
    public Result<IPage<SysLoginLog>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        return Result.success(loginLogService.selectPage(pageNum, pageSize, username, status));
    }

    @DeleteMapping("/{id}")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        loginLogService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    public Result<Void> clean() {
        loginLogService.clean();
        return Result.success();
    }
}
