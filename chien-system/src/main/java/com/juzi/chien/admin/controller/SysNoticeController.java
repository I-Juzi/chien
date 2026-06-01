package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.BusinessException;
import com.juzi.chien.admin.common.ErrorCode;
import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.domain.entity.SysNotice;
import com.juzi.chien.admin.security.LoginUser;
import com.juzi.chien.admin.service.SysNoticeService;
import com.juzi.chien.admin.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知公告", description = "通知公告的增删改查")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService noticeService;
    private final SseService sseService;

    @Operation(summary = "公告列表（管理端，含草稿）")
    @GetMapping("/list")
    public Result<List<SysNotice>> list() {
        return Result.success(noticeService.selectAll());
    }

    @Operation(summary = "已发布公告列表（前端展示用）")
    @GetMapping("/published")
    public Result<List<SysNotice>> published() {
        return Result.success(noticeService.selectPublished());
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public Result<SysNotice> detail(@PathVariable Long id) {
        SysNotice notice = noticeService.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        return Result.success(notice);
    }

    @Operation(summary = "新增公告")
    @PostMapping
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysNotice notice) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            notice.setCreateBy(loginUser.getUsername());
        } catch (Exception ignored) {}
        noticeService.insert(notice);
        // 发布状态时推送通知
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            sseService.broadcast("notice", notice);
        }
        return Result.success();
    }

    @Operation(summary = "修改公告")
    @PutMapping
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysNotice notice) {
        noticeService.update(notice);
        // 发布状态时推送通知
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            sseService.broadcast("notice", notice);
        }
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        noticeService.deleteById(id);
        return Result.success();
    }
}
