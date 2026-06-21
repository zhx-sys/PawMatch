package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("foster_order")
public class FosterOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long serviceId;
    private Long userId;
    private Long shelterId;
    private String petName;
    private String petType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private Double totalPrice;
    private String specialRequests;
    private Integer status;
    private Integer rating;
    private String comment;
    private LocalDateTime createTime;
}
