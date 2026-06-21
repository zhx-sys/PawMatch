package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.Post;
import com.pawmatch.dto.request.CreatePostRequest;
import com.pawmatch.dto.request.CreateCommentRequest;
import com.pawmatch.dto.request.PostQueryRequest;
import com.pawmatch.dto.response.PostResponse;
import com.pawmatch.dto.response.PostDetailResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface CommunityService extends IService<Post> {
    Long createPost(CreatePostRequest request, Long userId, Integer userType);
    IPage<PostResponse> getPostList(PostQueryRequest request, Long currentUserId);
    PostDetailResponse getPostDetail(Long postId, Long currentUserId);
    void deletePost(Long postId, Long userId);
    void likePost(Long postId, Long userId);
    void reviewPost(Long postId, boolean approved);
    IPage<PostResponse> getReviewList(PostQueryRequest request);
    Long createComment(CreateCommentRequest request, Long userId, Integer userType);
    void deleteComment(Long commentId, Long userId);
    void takeDownPost(Long postId);  // 救助站直接下架任意帖子
}
