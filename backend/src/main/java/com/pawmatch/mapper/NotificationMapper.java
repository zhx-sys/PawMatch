package com.pawmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawmatch.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}