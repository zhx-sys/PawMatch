package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("adoption_followup")
public class AdoptionFollowup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adoptionId;
    private Long userId;
    private Long shelterId;
    private String content;
    private String images;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String petName;
    @TableField(exist = false)
    private String userName;
}
