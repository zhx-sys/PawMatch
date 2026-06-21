package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.request.UpdateUserRequest;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.UserResponse;
import com.pawmatch.dto.response.PetResponse;
import com.pawmatch.dto.response.NotificationResponse;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/info")
    public ApiResponse<Void> updateUserInfo(@Valid @RequestBody UpdateUserRequest request) {
        Long userId = getCurrentUserId();
        userService.updateUserInfo(request, userId);
        return ApiResponse.success("更新成功", null);
    }

    @GetMapping("/info")
    public ApiResponse<UserResponse> getUserInfo() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        UserResponse response = userService.getUserInfo(principal.getUserId(), principal.getUserType());
        return ApiResponse.success(response);
    }

    @GetMapping("/pets")
    public ApiResponse<List<PetResponse>> getMyPets() {
        Long userId = getCurrentUserId();
        List<PetResponse> pets = userService.getMyPets(userId);
        return ApiResponse.success(pets);
    }

    @GetMapping("/notifications")
    public ApiResponse<IPage<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        IPage<NotificationResponse> page = userService.getNotifications(
                principal.getUserId(), principal.getUserType(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @PutMapping("/notification/{notificationId}/read")
    public ApiResponse<Void> markNotificationRead(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        userService.markNotificationRead(notificationId, userId);
        return ApiResponse.success("标记成功", null);
    }

    @PutMapping("/notifications/read-all")
    public ApiResponse<Void> markAllNotificationsRead() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        userService.markAllNotificationsRead(principal.getUserId(), principal.getUserType());
        return ApiResponse.success("全部已读", null);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
