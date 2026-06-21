package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailResponse {
    private Long id;
    private Long userId;
    private Integer userType;
    private String userName;
    private String title;
    private String content;
    private String category;
    private List<String> images;
    private Integer viewCount;
    private Integer likeCount;
    private List<CommentResponse> comments;
    private Integer commentCount;
    private Boolean hasLiked;
    private LocalDateTime createTime;
}