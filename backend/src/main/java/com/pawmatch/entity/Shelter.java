package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shelter")
public class Shelter {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String account;
    private String password;
    private String nickname;
    private String managerName;
    private String phone;
    private String province;
    private String city;
    private String addressDetail;
    private String introduction;
    private Integer creditScore;
    private Integer status;
    private Boolean infoComplete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
