package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.Badge;
import com.pawmatch.entity.UserPointsLog;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.GrowthService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/growth")
public class GrowthController {

    private final GrowthService growthService;

    public GrowthController(GrowthService growthService) {
        this.growthService = growthService;
    }

    @PostMapping("/checkin")
    public ApiResponse<Map<String, Object>> dailyCheckin() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        boolean success = growthService.dailyCheckin(principal.getUserId());
        if (!success) {
            return ApiResponse.error(400, "今日已签到");
        }
        Map<String, Object> level = growthService.getUserLevel(principal.getUserId());
        return ApiResponse.success("签到成功，+5积分", level);
    }

    @GetMapping("/my-points")
    public ApiResponse<Map<String, Object>> getMyPoints() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.success(growthService.getUserLevel(principal.getUserId()));
    }

    @GetMapping("/my-badges")
    public ApiResponse<List<Badge>> getMyBadges() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.success(growthService.getUserBadges(principal.getUserId()));
    }

    @GetMapping("/badges")
    public ApiResponse<List<Badge>> getAllBadges() {
        return ApiResponse.success(growthService.getAllBadges());
    }

    @GetMapping("/points-log")
    public ApiResponse<IPage<UserPointsLog>> getPointsLog(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.success(growthService.getPointsLog(principal.getUserId(), pageNum, pageSize));
    }
}