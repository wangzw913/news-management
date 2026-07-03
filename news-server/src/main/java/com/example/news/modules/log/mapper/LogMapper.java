package com.example.news.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.news.modules.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<OperationLog> {
}
