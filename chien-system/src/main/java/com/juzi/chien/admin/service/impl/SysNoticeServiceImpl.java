package com.juzi.chien.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juzi.chien.admin.domain.entity.SysNotice;
import com.juzi.chien.admin.mapper.SysNoticeMapper;
import com.juzi.chien.admin.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements SysNoticeService {

    private final SysNoticeMapper noticeMapper;

    @Override
    public List<SysNotice> selectAll() {
        return noticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>().orderByDesc(SysNotice::getCreateTime)
        );
    }

    @Override
    public List<SysNotice> selectPublished() {
        return noticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>()
                        .eq(SysNotice::getStatus, 1)
                        .orderByDesc(SysNotice::getCreateTime)
        );
    }

    @Override
    public SysNotice selectById(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public void insert(SysNotice notice) {
        noticeMapper.insert(notice);
    }

    @Override
    public void update(SysNotice notice) {
        noticeMapper.updateById(notice);
    }

    @Override
    public int deleteById(Long id) {
        return noticeMapper.deleteById(id);
    }
}
