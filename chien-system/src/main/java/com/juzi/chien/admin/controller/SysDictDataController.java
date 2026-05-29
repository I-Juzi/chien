package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.domain.entity.SysDictData;
import com.juzi.chien.admin.service.SysDictDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "字典数据", description = "字典数据的增删改查、按类型查询")
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService dictDataService;

    /**
     * 按字典类型查询数据（前端下拉框用）
     */
    @GetMapping("/type/{dictType}")
    public Result<List<SysDictData>> listByType(@PathVariable String dictType) {
        return Result.success(dictDataService.selectByDictType(dictType));
    }

    @GetMapping("/list")
    public Result<List<SysDictData>> list() {
        return Result.success(dictDataService.selectAll());
    }

    @GetMapping("/{id}")
    public Result<SysDictData> detail(@PathVariable Long id) {
        return Result.success(dictDataService.selectById(id));
    }

    @PostMapping
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysDictData dictData) {
        dictDataService.insert(dictData);
        return Result.success();
    }

    @PutMapping
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysDictData dictData) {
        dictDataService.update(dictData);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        dictDataService.deleteById(id);
        return Result.success();
    }
}
