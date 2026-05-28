package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juzi.chien.admin.domain.entity.SysDictType;
import com.juzi.chien.admin.mapper.SysDictTypeMapper;
import com.juzi.chien.admin.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl implements SysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;

    @Override
    public List<SysDictType> selectAll() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>().orderByAsc(SysDictType::getId));
    }

    @Override
    public SysDictType selectById(Long id) {
        return dictTypeMapper.selectById(id);
    }

    @Override
    public void insert(SysDictType dictType) {
        dictTypeMapper.insert(dictType);
    }

    @Override
    public void update(SysDictType dictType) {
        dictTypeMapper.updateById(dictType);
    }

    @Override
    public void deleteById(Long id) {
        dictTypeMapper.deleteById(id);
    }
}
