package com.example.news.modules.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.news.modules.news.entity.News;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsMapper extends BaseMapper<News> {
}
