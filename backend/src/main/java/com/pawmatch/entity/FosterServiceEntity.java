package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("foster_service")
public class FosterServiceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shelterId;
    private String title;
    private String description;
    private String petType;
    private Double pricePerDay;
    private Integer maxCapacity;
    private String availableDates;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
