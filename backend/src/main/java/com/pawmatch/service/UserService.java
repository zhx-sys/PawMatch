package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.User;
import com.pawmatch.dto.request.UpdateUserRequest;
import com.pawmatch.dto.response.UserResponse;
import com.pawmatch.dto.request.MatchingProfileRequest;

public interface UserService extends IService<User> {
    void updateUserInfo(UpdateUserRequest request, Long userId);
    UserResponse getUserInfo(Long userId, Integer userType);
    void updateMatchingProfile(Long userId, MatchingProfileRequest request);
    boolean checkInfoComplete(Long userId);
    java.util.List<com.pawmatch.dto.response.PetResponse> getMyPets(Long userId);
    com.baomidou.mybatisplus.core.metadata.IPage<com.pawmatch.dto.response.NotificationResponse> getNotifications(Long userId, Integer userType, long pageNum, long pageSize);
    void markNotificationRead(Long notificationId, Long userId);
    void markAllNotificationsRead(Long userId, Integer userType);
}
