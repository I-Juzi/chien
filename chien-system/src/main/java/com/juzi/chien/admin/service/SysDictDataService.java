package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysDictData;
import java.util.List;

public interface SysDictDataService {
    List<SysDictData> selectByDictType(String dictType);
    List<SysDictData> selectAll();
    SysDictData selectById(Long id);
    void insert(SysDictData dictData);
    void update(SysDictData dictData);
    void deleteById(Long id);
}
