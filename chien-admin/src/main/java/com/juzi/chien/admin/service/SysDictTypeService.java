package com.juzi.chien.admin.service;

import com.juzi.chien.admin.domain.entity.SysDictType;
import java.util.List;

public interface SysDictTypeService {
    List<SysDictType> selectAll();
    SysDictType selectById(Long id);
    void insert(SysDictType dictType);
    void update(SysDictType dictType);
    void deleteById(Long id);
}
