package com.pawmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawmatch.entity.WikiEntry;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WikiEntryMapper extends BaseMapper<WikiEntry> {
}