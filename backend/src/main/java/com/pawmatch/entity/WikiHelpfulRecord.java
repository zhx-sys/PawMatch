package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wiki_helpful_record")
public class WikiHelpfulRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long entryId;
    private Long userId;
    private LocalDateTime createTime;
}