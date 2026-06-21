package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credit_log")
public class CreditLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer userType;
    private Integer scoreChange;
    private Integer scoreAfter;
    private String reasonType;
    private String reasonDetail;
    private Long relatedId;
    private LocalDateTime createTime;
}