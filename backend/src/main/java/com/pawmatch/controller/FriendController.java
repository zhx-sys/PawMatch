package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.FriendResponse;
import com.pawmatch.entity.Friend;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.FriendMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendMapper friendMapper;

    @PostMapping("/request")
    public ApiResponse<Void> sendRequest(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        Integer userType = toInt(body.get("userType"));
        Long friendId = toLong(body.get("friendId"));
        Integer friendUserType = toInt(body.get("friendUserType"));
        friendService.sendRequest(userId, userType, friendId, friendUserType);
        return ApiResponse.success(null);
    }

    @PutMapping("/accept/{id}")
    public ApiResponse<Void> accept(@PathVariable Long id, @RequestParam Long userId) {
        friendService.acceptRequest(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/reject/{id}")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestParam Long userId) {
        friendService.rejectRequest(id, userId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        friendService.deleteFriend(userId, friendId);
        return ApiResponse.success(null);
    }

    @GetMapping("/list")
    public ApiResponse<List<FriendResponse>> getFriends(@RequestParam Long userId, @RequestParam Integer userType) {
        return ApiResponse.success(friendService.getFriends(userId, userType));
    }

    @GetMapping("/pending")
    public ApiResponse<List<FriendResponse>> getPending(@RequestParam Long userId, @RequestParam Integer userType) {
        return ApiResponse.success(friendService.getPendingRequests(userId, userType));
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> areFriends(@RequestParam Long userId, @RequestParam Long friendId) {
        return ApiResponse.success(friendService.areFriends(userId, friendId));
    }

    @GetMapping("/search")
    public ApiResponse<List<Map<String, Object>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam Long userId) {
        LambdaQueryWrapper<User> uw = new LambdaQueryWrapper<>();
        uw.like(User::getNickname, keyword).last("LIMIT 20");
        List<User> users = userMapper.selectList(uw);
        users = users.stream().filter(u -> !u.getId().equals(userId)).collect(Collectors.toList());

        // Get existing friend/pending relationships
        LambdaQueryWrapper<Friend> fw = new LambdaQueryWrapper<>();
        fw.eq(Friend::getUserId, userId).or().eq(Friend::getFriendId, userId);
        Set<Long> relatedIds = new HashSet<>();
        for (Friend f : friendMapper.selectList(fw)) {
            relatedIds.add(f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("userType", 0); // normal user
            m.put("added", relatedIds.contains(u.getId()));
            result.add(m);
        }
        return ApiResponse.success(result);
    }

    private Long toLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }

    private Integer toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        return null;
    }
}
