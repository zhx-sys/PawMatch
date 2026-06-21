package com.pawmatch.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.entity.Badge;
import com.pawmatch.entity.UserPointsLog;

import java.util.List;
import java.util.Map;

public interface GrowthService {
    void awardPoints(Long userId, int points, String action, String description);
    int getTotalPoints(Long userId);
    Map<String, Object> getUserLevel(Long userId);
    void checkAndAwardBadges(Long userId, Integer userType);
    List<Badge> getUserBadges(Long userId);
    List<Badge> getAllBadges();
    boolean dailyCheckin(Long userId);
    IPage<UserPointsLog> getPointsLog(Long userId, Integer pageNum, Integer pageSize);
}