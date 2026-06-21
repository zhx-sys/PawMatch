package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawmatch.dto.response.FriendResponse;
import com.pawmatch.entity.Friend;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.FriendMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.service.FriendService;
import com.pawmatch.service.MessagePushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessagePushService messagePushService;

    @Override
    public void sendRequest(Long userId, Integer userType, Long friendId, Integer friendUserType) {
        if (userId.equals(friendId)) return;

        // Check if already friends or pending
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        if (friendMapper.selectCount(w) > 0) return;

        // Check reverse direction too
        LambdaQueryWrapper<Friend> w2 = new LambdaQueryWrapper<>();
        w2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId);
        if (friendMapper.selectCount(w2) > 0) return;

        Friend f = new Friend();
        f.setUserId(userId);
        f.setUserType(userType);
        f.setFriendId(friendId);
        f.setFriendUserType(friendUserType);
        f.setStatus(0);
        f.setCreateTime(LocalDateTime.now());
        friendMapper.insert(f);

        // WebSocket 推送好友请求通知
        Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("requestId", f.getId());
        requestData.put("fromUserId", userId);
        requestData.put("fromUserType", userType);
        User fromUser = userMapper.selectById(userId);
        requestData.put("nickname", fromUser != null ? fromUser.getNickname() : "未知用户");
        messagePushService.pushNewFriendRequest(friendId, requestData);
    }

    @Override
    public void acceptRequest(Long requestId, Long currentUserId) {
        Friend f = friendMapper.selectById(requestId);
        if (f == null || !Objects.equals(f.getFriendId(), currentUserId)) return;
        f.setStatus(1);
        friendMapper.updateById(f);

        // WebSocket 推送好友请求被接受通知
        Map<String, Object> acceptData = new java.util.HashMap<>();
        acceptData.put("friendId", f.getUserId());
        acceptData.put("friendUserType", f.getUserType());
        User friendUser = userMapper.selectById(f.getUserId());
        acceptData.put("nickname", friendUser != null ? friendUser.getNickname() : "未知用户");
        messagePushService.pushFriendRequestAccepted(f.getUserId(), acceptData);
    }

    @Override
    public void rejectRequest(Long requestId, Long currentUserId) {
        Friend f = friendMapper.selectById(requestId);
        if (f == null || !Objects.equals(f.getFriendId(), currentUserId)) return;
        friendMapper.deleteById(requestId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        // 查找双向好友关系并删除
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getStatus, 1).and(wrapper ->
            wrapper.and(w1 -> w1.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                    .or(w2 -> w2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId))
        );
        Friend f = friendMapper.selectOne(w);
        if (f != null) {
            friendMapper.deleteById(f.getId());
        }
    }

    @Override
    public List<FriendResponse> getFriends(Long userId, Integer userType) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getStatus, 1).and(wrapper ->
            wrapper.eq(Friend::getUserId, userId).or().eq(Friend::getFriendId, userId)
        );
        List<Friend> list = friendMapper.selectList(w);
        return toResponse(list, userId);
    }

    @Override
    public List<FriendResponse> getPendingRequests(Long userId, Integer userType) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getStatus, 0).eq(Friend::getFriendId, userId);
        List<Friend> list = friendMapper.selectList(w);
        return toResponse(list, userId);
    }

    @Override
    public boolean areFriends(Long userId, Long friendId) {
        Friend f = friendMapper.findFriendship(userId, friendId);
        return f != null;
    }

    private List<FriendResponse> toResponse(List<Friend> list, Long myId) {
        Set<Long> userIds = list.stream().flatMap(f -> java.util.stream.Stream.of(f.getUserId(), f.getFriendId()))
                .filter(id -> !id.equals(myId)).collect(Collectors.toSet());

        java.util.Map<Long, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        List<FriendResponse> result = new ArrayList<>();
        for (Friend f : list) {
            FriendResponse r = new FriendResponse();
            r.setId(f.getId());
            r.setStatus(f.getStatus());
            r.setCreateTime(f.getCreateTime());

            Long otherId = f.getUserId().equals(myId) ? f.getFriendId() : f.getUserId();
            r.setFriendId(otherId);
            r.setFriendUserType(f.getUserId().equals(myId) ? f.getFriendUserType() : f.getUserType());

            User u = userMap.get(otherId);
            if (u != null) {
                r.setNickname(u.getNickname());
            }
            result.add(r);
        }
        return result;
    }
}
