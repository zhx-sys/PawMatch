package com.pawmatch.controller;

import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.NotificationResponse;
import com.pawmatch.entity.Notification;
import com.pawmatch.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationMapper notificationMapper;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list(@RequestParam Long userId, @RequestParam Integer userType) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getUserType, userType)
               .orderByDesc(Notification::getCreateTime);
        List<Notification> notifications = notificationMapper.selectList(wrapper);
        List<NotificationResponse> list = notifications.stream().map(n -> {
            NotificationResponse resp = new NotificationResponse();
            resp.setId(n.getId());
            resp.setType(n.getType());
            resp.setTitle(n.getTitle());
            resp.setContent(n.getContent());
            resp.setRelatedId(n.getRelatedId());
            resp.setIsRead(n.getIsRead());
            resp.setCreateTime(n.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Integer>> unreadCount(@RequestParam Long userId, @RequestParam Integer userType) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getUserType, userType)
               .eq(Notification::getIsRead, false);
        return ApiResponse.success(Map.of("count", Math.toIntExact(notificationMapper.selectCount(wrapper))));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setIsRead(true);
        notificationMapper.updateById(notification);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestParam Long userId, @RequestParam Integer userType) {
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Notification> wrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getUserType, userType)
               .eq(Notification::getIsRead, false)
               .set(Notification::getIsRead, true);
        notificationMapper.update(null, wrapper);
        return ApiResponse.success(null);
    }
}
