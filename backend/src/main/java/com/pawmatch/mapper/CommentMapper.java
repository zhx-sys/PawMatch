package com.pawmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawmatch.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}