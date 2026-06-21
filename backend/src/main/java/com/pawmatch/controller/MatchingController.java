package com.pawmatch.controller;

import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.MatchedPetResponse;
import com.pawmatch.dto.response.UserResponse;
import com.pawmatch.dto.request.MatchingProfileRequest;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.impl.MatchingService;
import com.pawmatch.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;
    private final UserService userService;

    public MatchingController(MatchingService matchingService, UserService userService) {
        this.matchingService = matchingService;
        this.userService = userService;
    }

    @GetMapping("/recommend")
    public ApiResponse<List<MatchedPetResponse>> recommend() {
        Long userId = getCurrentUserId();
        List<MatchedPetResponse> list = matchingService.recommend(userId, 20);
        return ApiResponse.success(list);
    }

    @GetMapping("/questionnaire")
    public ApiResponse<Map<String, Object>> questionnaire() {
        Map<String, Object> q = matchingService.getQuestionnaire();
        return ApiResponse.success(q);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> saveProfile(@RequestBody MatchingProfileRequest request) {
        Long userId = getCurrentUserId();
        userService.updateMatchingProfile(userId, request);
        return ApiResponse.success("保存成功", null);
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> getProfile() {
        Long userId = getCurrentUserId();
        UserResponse response = userService.getUserInfo(userId, 0);
        return ApiResponse.success(response);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}