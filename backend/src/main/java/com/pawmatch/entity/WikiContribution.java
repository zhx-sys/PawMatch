package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wiki_contribution")
public class WikiContribution {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long entryId;
    private String contribType;
    private Integer pointsAwarded;
    private LocalDateTime createTime;
}