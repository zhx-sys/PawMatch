package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawmatch.entity.Post;
import com.pawmatch.entity.Comment;
import com.pawmatch.entity.PostLike;
import com.pawmatch.mapper.PostMapper;
import com.pawmatch.mapper.CommentMapper;
import com.pawmatch.mapper.PostLikeMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.entity.User;
import com.pawmatch.service.CommunityService;
import com.pawmatch.service.MessagePushService;
import com.pawmatch.dto.request.CreatePostRequest;
import com.pawmatch.dto.request.CreateCommentRequest;
import com.pawmatch.dto.request.PostQueryRequest;
import com.pawmatch.dto.response.PostResponse;
import com.pawmatch.dto.response.PostDetailResponse;
import com.pawmatch.dto.response.CommentResponse;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.exception.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class CommunityServiceImpl extends ServiceImpl<PostMapper, Post> implements CommunityService {

    private final CommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final GrowthServiceImpl growthService;
    private final MessagePushService messagePushService;

    public CommunityServiceImpl(PostMapper postMapper, CommentMapper commentMapper, PostLikeMapper postLikeMapper,
                                UserMapper userMapper, ObjectMapper objectMapper, GrowthServiceImpl growthService,
                                MessagePushService messagePushService) {
        this.commentMapper = commentMapper;
        this.postLikeMapper = postLikeMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.growthService = growthService;
        this.messagePushService = messagePushService;
        this.baseMapper = postMapper;
    }

    @Override
    @Transactional
    public Long createPost(CreatePostRequest request, Long userId, Integer userType) {
        Post post = new Post();
        post.setUserId(userId);
        post.setUserType(userType);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setViewCount(0);
        post.setLikeCount(0);
        // 救助站(userType=1)发帖直接通过，普通用户(userType=0)发帖待审核
        post.setStatus(userType == 1 ? 1 : 2);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        try {
            if (request.getImages() != null) {
                post.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "JSON序列化失败");
        }
        save(post);

        // 积分：救助站发帖直接+3；普通用户待审核通过后再加
        if (userType == 1) {
            growthService.awardPoints(userId, 3, "POST_CREATE", "发布帖子");
        }

        return post.getId();
    }

    @Override
    public IPage<PostResponse> getPostList(PostQueryRequest request, Long currentUserId) {
        Page<Post> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Post> wrapper = Wrappers.lambdaQuery(Post.class);
        if (request.getStatus() != null) {
            wrapper.eq(Post::getStatus, request.getStatus());
        } else {
            wrapper.eq(Post::getStatus, 1);
        }
        wrapper.eq(request.getCategory() != null, Post::getCategory, request.getCategory())
               .like(request.getKeyword() != null, Post::getTitle, request.getKeyword());
        if ("like".equals(request.getSortBy())) {
            wrapper.orderByDesc(Post::getLikeCount);
        } else {
            wrapper.orderByDesc(Post::getCreateTime);
        }
        IPage<Post> result = page(page, wrapper);

        // Batch load user nicknames
        Set<Long> userIds = result.getRecords().stream()
                .map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, String> nicknameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectList(Wrappers.lambdaQuery(User.class).in(User::getId, userIds))
                    .forEach(u -> nicknameMap.put(u.getId(), u.getNickname()));
        }

        IPage<PostResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(p -> toPostResponse(p, currentUserId, nicknameMap.get(p.getUserId())))
                .collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    public PostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        Post post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);

        // 成长激励：帖子每满100浏览+10积分
        int newViews = post.getViewCount();
        if (newViews > 0 && newViews % 100 == 0) {
            growthService.awardPoints(post.getUserId(), 10, "POST_VIEW_MILESTONE",
                    "帖子《" + post.getTitle() + "》浏览达到" + newViews + "次");
            growthService.checkAndAwardBadges(post.getUserId(), post.getUserType());
        }

        PostDetailResponse detail = new PostDetailResponse();
        BeanUtils.copyProperties(post, detail, "images");
        detail.setCreateTime(post.getCreateTime());
        detail.setHasLiked(hasLiked(postId, currentUserId));
        // Fill user nickname
        User author = userMapper.selectById(post.getUserId());
        detail.setUserName(author != null ? author.getNickname() : null);
        try {
            if (post.getImages() != null) {
                detail.setImages(objectMapper.readValue(post.getImages(), List.class));
            }
        } catch (JsonProcessingException ignored) {}

        LambdaQueryWrapper<Comment> commentWrapper = Wrappers.lambdaQuery(Comment.class)
                .eq(Comment::getPostId, postId)
                .orderByAsc(Comment::getCreateTime);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        // Batch load comment user nicknames
        Set<Long> commentUserIds = comments.stream()
                .map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, String> commentNicknameMap = new HashMap<>();
        if (!commentUserIds.isEmpty()) {
            userMapper.selectList(Wrappers.lambdaQuery(User.class).in(User::getId, commentUserIds))
                    .forEach(u -> commentNicknameMap.put(u.getId(), u.getNickname()));
        }

        List<CommentResponse> commentTree = buildCommentTree(comments, commentNicknameMap);
        detail.setComments(commentTree);
        detail.setCommentCount(comments.size());
        return detail;
    }

    private List<CommentResponse> buildCommentTree(List<Comment> comments, Map<Long, String> nicknameMap) {
        List<CommentResponse> topLevel = comments.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> toCommentResponse(c, nicknameMap))
                .collect(Collectors.toList());
        for (CommentResponse top : topLevel) {
            List<CommentResponse> replies = comments.stream()
                    .filter(c -> c.getParentId() != null && c.getParentId().equals(top.getId()))
                    .map(c -> toCommentResponse(c, nicknameMap))
                    .collect(Collectors.toList());
            top.setReplies(replies);
        }
        return topLevel;
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        post.setStatus(0);
        updateById(post);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        LambdaQueryWrapper<PostLike> lqw = Wrappers.lambdaQuery(PostLike.class)
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        PostLike existing = postLikeMapper.selectOne(lqw);
        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            PostLike pl = new PostLike();
            pl.setPostId(postId);
            pl.setUserId(userId);
            pl.setCreateTime(LocalDateTime.now());
            postLikeMapper.insert(pl);
            post.setLikeCount(post.getLikeCount() + 1);

            // 积分：帖子被点赞，作者+1
            growthService.awardPoints(post.getUserId(), 1, "POST_LIKED", "帖子被点赞");
        }
        updateById(post);
    }

    @Override
    @Transactional
    public void reviewPost(Long postId, boolean approved) {
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if (post.getStatus() != 2) {
            throw new BusinessException(400, "该帖子不是待审核状态");
        }
        post.setStatus(approved ? 1 : 0); // 0=审核不通过
        post.setUpdateTime(LocalDateTime.now());
        updateById(post);

        // 积分：帖子审核通过+3
        if (approved) {
            growthService.awardPoints(post.getUserId(), 3, "POST_CREATE", "帖子审核通过");
        }
    }

    @Override
    public IPage<PostResponse> getReviewList(PostQueryRequest request) {
        request.setStatus(2);
        return getPostList(request, null);
    }

    @Override
    @Transactional
    public Long createComment(CreateCommentRequest request, Long userId, Integer userType) {
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setUserType(userType);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        // WebSocket 推送新评论给帖子作者
        try {
            Post post = getById(request.getPostId());
            if (post != null && !post.getUserId().equals(userId)) {
                Map<String, Object> pushData = new java.util.HashMap<>();
                pushData.put("commentId", comment.getId());
                pushData.put("postId", request.getPostId());
                pushData.put("postTitle", post.getTitle());
                pushData.put("content", request.getContent());
                pushData.put("fromUserId", userId);
                User commenter = userMapper.selectById(userId);
                pushData.put("fromNickname", commenter != null ? commenter.getNickname() : "未知用户");
                messagePushService.pushNewComment(post.getUserId(), pushData);
            }
        } catch (Exception ignored) {}

        return comment.getId();
    }

    @Override
    @Transactional
    public void takeDownPost(Long postId) {
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        post.setStatus(0);
        post.setUpdateTime(LocalDateTime.now());
        updateById(post);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        commentMapper.deleteById(commentId);
    }

    private boolean hasLiked(Long postId, Long userId) {
        if (userId == null) return false;
        LambdaQueryWrapper<PostLike> lqw = Wrappers.lambdaQuery(PostLike.class)
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        return postLikeMapper.selectCount(lqw) > 0;
    }

    private PostResponse toPostResponse(Post post, Long currentUserId, String nickname) {
        PostResponse r = new PostResponse();
        BeanUtils.copyProperties(post, r, "content", "images");
        r.setUserName(nickname);
        r.setCreateTime(post.getCreateTime());
        r.setHasLiked(hasLiked(post.getId(), currentUserId));
        if (post.getContent() != null && post.getContent().length() > 100) {
            r.setContent(post.getContent().substring(0, 100) + "...");
        } else {
            r.setContent(post.getContent());
        }
        try {
            if (post.getImages() != null) {
                List<?> imgList = objectMapper.readValue(post.getImages(), List.class);
                r.setImages((List<String>) imgList);
            }
        } catch (JsonProcessingException ignored) {}
        LambdaQueryWrapper<Comment> wrapper = Wrappers.lambdaQuery(Comment.class)
                .eq(Comment::getPostId, post.getId());
        r.setCommentCount(commentMapper.selectCount(wrapper).intValue());
        return r;
    }

    private CommentResponse toCommentResponse(Comment c, Map<Long, String> nicknameMap) {
        CommentResponse r = new CommentResponse();
        BeanUtils.copyProperties(c, r);
        r.setUserName(nicknameMap.get(c.getUserId()));
        r.setCreateTime(c.getCreateTime());
        return r;
    }
}
