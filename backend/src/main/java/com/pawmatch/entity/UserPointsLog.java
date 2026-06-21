package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_points_log")
public class UserPointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer points;
    private String action;
    private String description;
    private LocalDateTime createTime;
}