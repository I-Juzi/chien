package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juzi.chien.admin.domain.entity.SysDictData;
import com.juzi.chien.admin.mapper.SysDictDataMapper;
import com.juzi.chien.admin.service.SysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl implements SysDictDataService {

    private final SysDictDataMapper dictDataMapper;

    @Override
    public List<SysDictData> selectByDictType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSortOrder));
    }

    @Override
    public List<SysDictData> selectAll() {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>().orderByAsc(SysDictData::getSortOrder));
    }

    @Override
    public SysDictData selectById(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public void insert(SysDictData dictData) {
        dictDataMapper.insert(dictData);
    }

    @Override
    public void update(SysDictData dictData) {
        dictDataMapper.updateById(dictData);
    }

    @Override
    public void deleteById(Long id) {
        dictDataMapper.deleteById(id);
    }
}
