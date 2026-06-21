package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawmatch.entity.*;
import com.pawmatch.mapper.*;
import com.pawmatch.service.GrowthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GrowthServiceImpl implements GrowthService {

    private final UserPointsLogMapper userPointsLogMapper;
    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;
    private final UserMapper userMapper;
    private final AdoptionApplicationMapper adoptionApplicationMapper;
    private final WikiEntryMapper wikiEntryMapper;
    private final WikiContributionMapper wikiContributionMapper;
    private final PostMapper postMapper;

    public GrowthServiceImpl(UserPointsLogMapper userPointsLogMapper, BadgeMapper badgeMapper,
                             UserBadgeMapper userBadgeMapper, UserMapper userMapper,
                             AdoptionApplicationMapper adoptionApplicationMapper,
                             WikiEntryMapper wikiEntryMapper, WikiContributionMapper wikiContributionMapper,
                             PostMapper postMapper) {
        this.userPointsLogMapper = userPointsLogMapper;
        this.badgeMapper = badgeMapper;
        this.userBadgeMapper = userBadgeMapper;
        this.userMapper = userMapper;
        this.adoptionApplicationMapper = adoptionApplicationMapper;
        this.wikiEntryMapper = wikiEntryMapper;
        this.wikiContributionMapper = wikiContributionMapper;
        this.postMapper = postMapper;
    }

    @Override
    @Transactional
    public void awardPoints(Long userId, int points, String action, String description) {
        UserPointsLog log = new UserPointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setAction(action);
        log.setDescription(description);
        log.setCreateTime(LocalDateTime.now());
        userPointsLogMapper.insert(log);
    }

    @Override
    public int getTotalPoints(Long userId) {
        LambdaQueryWrapper<UserPointsLog> wrapper = Wrappers.lambdaQuery(UserPointsLog.class)
                .eq(UserPointsLog::getUserId, userId);
        List<UserPointsLog> logs = userPointsLogMapper.selectList(wrapper);
        return logs.stream().mapToInt(l -> l.getPoints() != null ? l.getPoints() : 0).sum();
    }

    @Override
    public Map<String, Object> getUserLevel(Long userId) {
        int total = getTotalPoints(userId);
        Map<String, Object> result = buildLevelInfo(total);
        int consecutiveDays = calcConsecutiveCheckinDays(userId);
        result.put("consecutiveDays", consecutiveDays);
        return result;
    }

    private int calcConsecutiveCheckinDays(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        // 从昨天开始回溯（今天可能还没签到）
        for (int i = 0; i <= 365; i++) {
            LocalDate thatDay = today.minusDays(i);
            LocalDateTime dayStart = thatDay.atStartOfDay();
            LocalDateTime dayEnd = thatDay.plusDays(1).atStartOfDay();
            LambdaQueryWrapper<UserPointsLog> wrapper = Wrappers.lambdaQuery(UserPointsLog.class)
                    .eq(UserPointsLog::getUserId, userId)
                    .eq(UserPointsLog::getAction, "DAILY_CHECKIN")
                    .ge(UserPointsLog::getCreateTime, dayStart)
                    .lt(UserPointsLog::getCreateTime, dayEnd);
            if (userPointsLogMapper.selectCount(wrapper) > 0) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private Map<String, Object> buildLevelInfo(int totalPoints) {
        int level;
        String levelName;
        int nextLevelPoints;
        if (totalPoints < 100) {
            level = 1;
            levelName = "萌新";
            nextLevelPoints = 100;
        } else if (totalPoints < 300) {
            level = 2;
            levelName = "熟手";
            nextLevelPoints = 300;
        } else if (totalPoints < 700) {
            level = 3;
            levelName = "铲屎达人";
            nextLevelPoints = 700;
        } else if (totalPoints < 1500) {
            level = 4;
            levelName = "养宠专家";
            nextLevelPoints = 1500;
        } else {
            level = 5;
            levelName = "社区长老";
            nextLevelPoints = totalPoints; // already max
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("level", level);
        result.put("levelName", levelName);
        result.put("currentPoints", totalPoints);
        result.put("nextLevelPoints", nextLevelPoints);
        return result;
    }

    @Override
    @Transactional
    public void checkAndAwardBadges(Long userId, Integer userType) {
        // 1. 新晋铲屎官: 首次领养成功 (adoption_application status=3且count=1)
        LambdaQueryWrapper<AdoptionApplication> appWrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getUserId, userId)
                .eq(AdoptionApplication::getStatus, 3);
        long completedCount = adoptionApplicationMapper.selectCount(appWrapper);
        if (completedCount >= 1) {
            awardBadgeIfNotOwned(userId, 1L); // id=1: 新晋铲屎官
        }

        // 2. 百科达人: 贡献(wiki_contribution)达10条且对应entry status=1
        LambdaQueryWrapper<WikiContribution> contribWrapper = Wrappers.lambdaQuery(WikiContribution.class)
                .eq(WikiContribution::getUserId, userId);
        long contribCount = wikiContributionMapper.selectCount(contribWrapper);
        if (contribCount >= 10) {
            awardBadgeIfNotOwned(userId, 2L); // id=2: 百科达人
        }

        // 3. 社区元老: 注册满365天
        User user = userMapper.selectById(userId);
        if (user != null && user.getCreateTime() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(user.getCreateTime(), LocalDateTime.now());
            if (days >= 365) {
                awardBadgeIfNotOwned(userId, 4L); // id=4: 社区元老
            }
        }

        // 4. 救助站之光: 成功送出50只(仅救助站userType=1)
        if (userType != null && userType == 1) {
            LambdaQueryWrapper<AdoptionApplication> shelterWrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                    .eq(AdoptionApplication::getShelterId, userId)
                    .eq(AdoptionApplication::getStatus, 3);
            long shelterCompleted = adoptionApplicationMapper.selectCount(shelterWrapper);
            if (shelterCompleted >= 50) {
                awardBadgeIfNotOwned(userId, 5L); // id=5: 救助站之光
            }
        }

        // 5. 满分信用: 信用分>=100
        if (user != null && user.getCreditScore() != null && user.getCreditScore() >= 100) {
            awardBadgeIfNotOwned(userId, 6L); // id=6: 满分信用
        }

        // 6. 爆款作者: 帖子浏览量>=1000
        LambdaQueryWrapper<Post> postWrapper = Wrappers.lambdaQuery(Post.class)
                .eq(Post::getUserId, userId)
                .ge(Post::getViewCount, 1000);
        long viralPostCount = postMapper.selectCount(postWrapper);
        if (viralPostCount > 0) {
            awardBadgeIfNotOwned(userId, 7L); // id=7: 爆款作者
        }

        // 7. 铲屎百事通: 百科被标记有帮助>=50
        LambdaQueryWrapper<WikiEntry> wikiWrapper = Wrappers.lambdaQuery(WikiEntry.class)
                .eq(WikiEntry::getAuthorId, userId)
                .ge(WikiEntry::getHelpfulCount, 50);
        long helpfulWikiCount = wikiEntryMapper.selectCount(wikiWrapper);
        if (helpfulWikiCount > 0) {
            awardBadgeIfNotOwned(userId, 8L); // id=8: 铲屎百事通
        }
    }

    private void awardBadgeIfNotOwned(Long userId, Long badgeId) {
        LambdaQueryWrapper<UserBadge> wrapper = Wrappers.lambdaQuery(UserBadge.class)
                .eq(UserBadge::getUserId, userId)
                .eq(UserBadge::getBadgeId, badgeId);
        if (userBadgeMapper.selectCount(wrapper) == 0) {
            UserBadge ub = new UserBadge();
            ub.setUserId(userId);
            ub.setBadgeId(badgeId);
            ub.setAwardedTime(LocalDateTime.now());
            userBadgeMapper.insert(ub);
        }
    }

    @Override
    public List<Badge> getUserBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeMapper.selectList(
                Wrappers.lambdaQuery(UserBadge.class).eq(UserBadge::getUserId, userId));
        if (userBadges.isEmpty()) return Collections.emptyList();
        List<Long> badgeIds = userBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toList());
        return badgeMapper.selectList(Wrappers.lambdaQuery(Badge.class).in(Badge::getId, badgeIds));
    }

    @Override
    public List<Badge> getAllBadges() {
        return badgeMapper.selectList(Wrappers.lambdaQuery(Badge.class));
    }

    @Override
    @Transactional
    public boolean dailyCheckin(Long userId) {
        // 检查今天是否已签到
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<UserPointsLog> wrapper = Wrappers.lambdaQuery(UserPointsLog.class)
                .eq(UserPointsLog::getUserId, userId)
                .eq(UserPointsLog::getAction, "DAILY_CHECKIN")
                .ge(UserPointsLog::getCreateTime, todayStart)
                .lt(UserPointsLog::getCreateTime, todayEnd);
        if (userPointsLogMapper.selectCount(wrapper) > 0) {
            return false; // 已签到
        }

        // 计算连续签到天数：往前回溯
        int streak = 1;
        for (int i = 1; i <= 365; i++) {
            LocalDate thatDay = today.minusDays(i);
            LocalDateTime dayStart = thatDay.atStartOfDay();
            LocalDateTime dayEnd = thatDay.plusDays(1).atStartOfDay();
            LambdaQueryWrapper<UserPointsLog> dayWrapper = Wrappers.lambdaQuery(UserPointsLog.class)
                    .eq(UserPointsLog::getUserId, userId)
                    .eq(UserPointsLog::getAction, "DAILY_CHECKIN")
                    .ge(UserPointsLog::getCreateTime, dayStart)
                    .lt(UserPointsLog::getCreateTime, dayEnd);
            if (userPointsLogMapper.selectCount(dayWrapper) > 0) {
                streak++;
            } else {
                break;
            }
        }

        // 阶梯奖励：连续1-2天+5，3-6天+7，7-13天+10，14天以上+15
        int points;
        if (streak >= 14) {
            points = 15;
        } else if (streak >= 7) {
            points = 10;
        } else if (streak >= 3) {
            points = 7;
        } else {
            points = 5;
        }

        awardPoints(userId, points, "DAILY_CHECKIN", "每日签到(连续" + streak + "天)");
        return true;
    }

    @Override
    public IPage<UserPointsLog> getPointsLog(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserPointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserPointsLog> wrapper = Wrappers.lambdaQuery(UserPointsLog.class)
                .eq(UserPointsLog::getUserId, userId)
                .orderByDesc(UserPointsLog::getCreateTime);
        return userPointsLogMapper.selectPage(page, wrapper);
    }
}