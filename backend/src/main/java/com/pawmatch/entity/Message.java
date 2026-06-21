package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private Integer fromUserType;
    private Long toUserId;
    private Integer toUserType;
    private Long adoptionId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
