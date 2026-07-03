package com.example.news.modules.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.news.modules.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
