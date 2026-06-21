package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String account;
    private String password;
    private String nickname;
    private String realName;
    private String idCard;
    private String phone;
    private String province;
    private String city;
    private String addressDetail;
    private String petPreference;
    private String livingSpace;
    private Boolean hasChildren;
    private Boolean hasOtherPets;
    private String petExperience;
    private String dailyRoutine;
    private String budgetRange;
    private LocalDate birthday;
    private Boolean matchingProfileComplete;
    private Integer userType;
    private Integer creditScore;
    private Integer status;
    private Boolean infoComplete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
