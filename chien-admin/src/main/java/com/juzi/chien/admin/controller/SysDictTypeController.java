package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.domain.entity.SysDictType;
import com.juzi.chien.admin.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "字典类型", description = "字典类型的增删改查")
@RestController
@RequestMapping("/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;

    @GetMapping("/list")
    public Result<List<SysDictType>> list() {
        return Result.success(dictTypeService.selectAll());
    }

    @GetMapping("/{id}")
    public Result<SysDictType> detail(@PathVariable Long id) {
        return Result.success(dictTypeService.selectById(id));
    }

    @PostMapping
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysDictType dictType) {
        dictTypeService.insert(dictType);
        return Result.success();
    }

    @PutMapping
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysDictType dictType) {
        dictTypeService.update(dictType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        dictTypeService.deleteById(id);
        return Result.success();
    }
}
