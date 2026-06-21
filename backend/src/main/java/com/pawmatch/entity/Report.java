package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private String targetType;  // POST / COMMENT
    private Long targetId;
    private String reason;
    private Integer status;  // 0=待处理 1=已处理 2=已驳回
    private LocalDateTime createTime;
}
