package com.pawmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawmatch.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT * FROM friend WHERE ((user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})) AND status = 1")
    Friend findFriendship(Long userId, Long friendId);
}
