package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juzi.chien.admin.domain.entity.SysFileRecord;
import com.juzi.chien.admin.mapper.SysFileRecordMapper;
import com.juzi.chien.admin.service.SysFileRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysFileRecordServiceImpl implements SysFileRecordService {

    private final SysFileRecordMapper fileRecordMapper;

    @Override
    public void insert(SysFileRecord record) {
        fileRecordMapper.insert(record);
    }

    @Override
    public List<SysFileRecord> selectAll() {
        return fileRecordMapper.selectList(
                new LambdaQueryWrapper<SysFileRecord>().orderByDesc(SysFileRecord::getCreateTime)
        );
    }

    @Override
    public SysFileRecord selectById(Long id) {
        return fileRecordMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return fileRecordMapper.deleteById(id);
    }
}
