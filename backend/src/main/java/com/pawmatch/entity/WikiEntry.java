package com.pawmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wiki_entry")
public class WikiEntry {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private Integer status;
    private Long authorId;
    private Integer viewCount;
    private Integer helpfulCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}