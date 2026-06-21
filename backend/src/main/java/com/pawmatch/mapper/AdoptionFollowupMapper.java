package com.pawmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawmatch.entity.AdoptionFollowup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdoptionFollowupMapper extends BaseMapper<AdoptionFollowup> {

    @Select("SELECT f.*, p.name as pet_name, u.nickname as user_name " +
            "FROM adoption_followup f " +
            "LEFT JOIN adoption_application a ON f.adoption_id = a.id " +
            "LEFT JOIN pet p ON a.pet_id = p.id " +
            "LEFT JOIN `user` u ON a.user_id = u.id " +
            "WHERE f.shelter_id = #{shelterId} " +
            "ORDER BY f.create_time DESC")
    @Results({
        @Result(column = "pet_name", property = "petName"),
        @Result(column = "user_name", property = "userName")
    })
    List<AdoptionFollowup> selectByShelterIdWithJoin(@Param("shelterId") Long shelterId);
}
