package com.pawmatch.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotNull(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}