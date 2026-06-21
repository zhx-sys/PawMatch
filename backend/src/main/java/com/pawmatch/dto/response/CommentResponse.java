package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private Integer userType;
    private String userName;
    private String content;
    private Long parentId;
    private List<CommentResponse> replies;
    private LocalDateTime createTime;
}