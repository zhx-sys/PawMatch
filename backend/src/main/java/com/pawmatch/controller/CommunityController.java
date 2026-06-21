package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.request.CreatePostRequest;
import com.pawmatch.dto.request.CreateCommentRequest;
import com.pawmatch.dto.request.PostQueryRequest;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.PostResponse;
import com.pawmatch.dto.response.PostDetailResponse;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.CommunityService;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/post")
    public ApiResponse<Long> createPost(@Valid @RequestBody CreatePostRequest request) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long id = communityService.createPost(request, principal.getUserId(), principal.getUserType());
        String msg = principal.getUserType() == 1 ? "发布成功" : "发布成功，等待审核";
        return ApiResponse.success(msg, id);
    }

    @GetMapping("/post/list")
    public ApiResponse<IPage<PostResponse>> getPostList(@ModelAttribute PostQueryRequest request) {
        Long userId = getCurrentUserIdQuietly();
        IPage<PostResponse> page = communityService.getPostList(request, userId);
        return ApiResponse.success(page);
    }

    @GetMapping("/post/review/list")
    public ApiResponse<IPage<PostResponse>> getReviewList(@ModelAttribute PostQueryRequest request) {
        IPage<PostResponse> page = communityService.getReviewList(request);
        return ApiResponse.success(page);
    }

    @GetMapping("/post/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        Long userId = getCurrentUserIdQuietly();
        PostDetailResponse detail = communityService.getPostDetail(postId, userId);
        return ApiResponse.success(detail);
    }

    @DeleteMapping("/post/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        Long userId = getCurrentUserId();
        communityService.deletePost(postId, userId);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/post/{postId}/like")
    public ApiResponse<Boolean> likePost(@PathVariable Long postId) {
        Long userId = getCurrentUserId();
        communityService.likePost(postId, userId);
        return ApiResponse.success("操作成功", true);
    }

    @PutMapping("/post/{postId}/take-down")
    public ApiResponse<Void> takeDownPost(@PathVariable Long postId) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal.getUserType() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        communityService.takeDownPost(postId);
        return ApiResponse.success("帖子已下架", null);
    }

    @PutMapping("/post/{postId}/review")
    public ApiResponse<Void> reviewPost(@PathVariable Long postId, @RequestBody Map<String, Boolean> body) {
        Boolean approved = body.getOrDefault("approved", true);
        communityService.reviewPost(postId, approved);
        return ApiResponse.success(approved ? "审核通过" : "已拒绝", null);
    }

    @PostMapping("/comment")
    public ApiResponse<Long> createComment(@Valid @RequestBody CreateCommentRequest request) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long id = communityService.createComment(request, principal.getUserId(), principal.getUserType());
        return ApiResponse.success("评论成功", id);
    }

    @DeleteMapping("/comment/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = getCurrentUserId();
        communityService.deleteComment(commentId, userId);
        return ApiResponse.success("删除成功", null);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }

    private Long getCurrentUserIdQuietly() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof PawMatchPrincipal principal) {
                return principal.getUserId();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
