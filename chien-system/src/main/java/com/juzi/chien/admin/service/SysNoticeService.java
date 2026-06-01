package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysNotice;

import java.util.List;

public interface SysNoticeService {

    List<SysNotice> selectAll();

    List<SysNotice> selectPublished();

    SysNotice selectById(Long id);

    void insert(SysNotice notice);

    void update(SysNotice notice);

    int deleteById(Long id);
}
