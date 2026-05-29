package com.juzi.chien.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.juzi.chien.admin.domain.entity.SysOperLog;
import com.juzi.chien.admin.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Controller
 */
@Tag(name = "操作日志", description = "操作日志查询、删除、清空")
@RestController
@RequestMapping("/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/list")
    public Result<IPage<SysOperLog>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Integer status) {
        IPage<SysOperLog> page = operLogService.selectLogPage(pageNum, pageSize, title, businessType, status);
        return Result.success(page);
    }

    /**
     * 查询日志详情
     */
    @GetMapping("/{id}")
    public Result<SysOperLog> detail(@PathVariable Long id) {
        return Result.success(operLogService.selectLogById(id));
    }

    /**
     * 删除日志
     */
    @DeleteMapping("/{id}")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        operLogService.deleteLogById(id);
        return Result.success();
    }

    /**
     * 清空日志
     */
    @DeleteMapping("/clean")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    public Result<Void> clean() {
        operLogService.cleanLog();
        return Result.success();
    }
}
