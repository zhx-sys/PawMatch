package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shelterId;
    private String name;
    private String type;
    private String breed;
    private String gender;
    private Integer age;
    private String color;
    private Double weight;
    private String healthStatus;
    private Boolean vaccinated;
    private Boolean sterilized;
    private String sizeLevel;
    private String activityLevel;
    private String sheddingLevel;
    private Boolean socialized;
    private Boolean beginnerFriendly;
    private Boolean needsYard;
    private Boolean goodWithKids;
    private Boolean goodWithPets;
    private String description;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
