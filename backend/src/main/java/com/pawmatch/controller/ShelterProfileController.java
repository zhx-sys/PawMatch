package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.*;
import com.pawmatch.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shelter")
public class ShelterProfileController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final AdoptionApplicationMapper adoptionApplicationMapper;
    private final PostMapper postMapper;

    public ShelterProfileController(UserMapper userMapper, PetMapper petMapper,
                                    AdoptionApplicationMapper adoptionApplicationMapper,
                                    PostMapper postMapper) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.adoptionApplicationMapper = adoptionApplicationMapper;
        this.postMapper = postMapper;
    }

    @GetMapping("/{shelterId}/profile")
    public ApiResponse<Map<String, Object>> getProfile(@PathVariable Long shelterId) {
        User shelter = userMapper.selectById(shelterId);
        if (shelter == null) {
            return ApiResponse.error(404, "救助站不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // shelterInfo
        Map<String, Object> shelterInfo = new LinkedHashMap<>();
        shelterInfo.put("id", shelter.getId());
        shelterInfo.put("nickname", shelter.getNickname());
        result.put("shelterInfo", shelterInfo);

        // stats
        LambdaQueryWrapper<AdoptionApplication> appWrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getShelterId, shelterId);
        List<AdoptionApplication> allApps = adoptionApplicationMapper.selectList(appWrapper);

        long totalAdopted = allApps.stream().filter(a -> a.getStatus() != null && a.getStatus() == 3).count();
        long totalApplied = allApps.size();
        long rejectedCount = allApps.stream().filter(a -> a.getStatus() != null && a.getStatus() == 2).count();
        double successRate = totalApplied > 0 ? (double) totalAdopted / totalApplied : 0.0;

        // avgResponseHours
        double avgResponseHours = allApps.stream()
                .filter(a -> a.getApplyTime() != null && a.getAuditTime() != null)
                .mapToLong(a -> ChronoUnit.HOURS.between(a.getApplyTime(), a.getAuditTime()))
                .average().orElse(0.0);

        long currentPets = petMapper.selectCount(
                Wrappers.lambdaQuery(Pet.class)
                        .eq(Pet::getShelterId, shelterId)
                        .eq(Pet::getStatus, 0));

        // returnRate (简单计算：已退回数/总数；退回指 status=4，如果没有则默认为0)
        long returnedCount = allApps.stream().filter(a -> a.getStatus() != null && a.getStatus() == 4).count();
        double returnRate = totalApplied > 0 ? (double) returnedCount / totalApplied : 0.0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAdopted", totalAdopted);
        stats.put("currentPets", currentPets);
        stats.put("successRate", Math.round(successRate * 10000.0) / 10000.0);
        stats.put("avgResponseHours", Math.round(avgResponseHours * 10.0) / 10.0);
        stats.put("returnRate", Math.round(returnRate * 10000.0) / 10000.0);
        result.put("stats", stats);

        // recentStories: 审核通过领养对应的帖子(category='领养故事')
        Set<Long> adoptedUserIds = allApps.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 3)
                .map(AdoptionApplication::getUserId)
                .collect(Collectors.toSet());

        List<Post> stories = new ArrayList<>();
        if (!adoptedUserIds.isEmpty()) {
            stories = postMapper.selectList(
                    Wrappers.lambdaQuery(Post.class)
                            .in(Post::getUserId, adoptedUserIds)
                            .eq(Post::getCategory, "领养故事")
                            .eq(Post::getStatus, 1)
                            .orderByDesc(Post::getCreateTime)
                            .last("LIMIT 5"));
        }

        List<Map<String, Object>> recentStories = stories.stream().map(p -> {
            Map<String, Object> sm = new LinkedHashMap<>();
            sm.put("id", p.getId());
            sm.put("title", p.getTitle());
            sm.put("createTime", p.getCreateTime());
            sm.put("viewCount", p.getViewCount());
            return sm;
        }).collect(Collectors.toList());
        result.put("recentStories", recentStories);

        return ApiResponse.success(result);
    }

    @GetMapping("/ranking")
    public ApiResponse<List<Map<String, Object>>> getRanking() {
        // 获取所有救助站 (需要查询userType=1的用户)
        LambdaQueryWrapper<AdoptionApplication> allWrapper = Wrappers.lambdaQuery(AdoptionApplication.class);
        List<AdoptionApplication> allApps = adoptionApplicationMapper.selectList(allWrapper);

        // 按shelterId分组
        Map<Long, List<AdoptionApplication>> grouped = allApps.stream()
                .collect(Collectors.groupingBy(AdoptionApplication::getShelterId));

        // 计算每个救助站得分
        double maxAdopted = grouped.values().stream()
                .mapToLong(list -> list.stream().filter(a -> a.getStatus() == 3).count())
                .max().orElse(1);

        List<Map<String, Object>> rankings = new ArrayList<>();
        for (Map.Entry<Long, List<AdoptionApplication>> entry : grouped.entrySet()) {
            Long shelterId = entry.getKey();
            List<AdoptionApplication> apps = entry.getValue();
            long totalAdopted = apps.stream().filter(a -> a.getStatus() == 3).count();
            long total = apps.size();
            long returned = apps.stream().filter(a -> a.getStatus() == 4).count();

            double successRate = total > 0 ? (double) totalAdopted / total : 0.0;
            double returnRate = total > 0 ? (double) returned / total : 0.0;
            double normalizedAdopted = maxAdopted > 0 ? totalAdopted / maxAdopted : 0.0;
            double score = successRate * 0.4 + normalizedAdopted * 0.3 + (1.0 - returnRate) * 0.3;

            User shelter = userMapper.selectById(shelterId);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("shelterId", shelterId);
            item.put("nickname", shelter != null ? shelter.getNickname() : "未知");
            item.put("totalAdopted", totalAdopted);
            item.put("successRate", Math.round(successRate * 10000.0) / 10000.0);
            item.put("score", Math.round(score * 10000.0) / 10000.0);
            rankings.add(item);
        }

        rankings.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        List<Map<String, Object>> top20 = rankings.size() > 20 ? rankings.subList(0, 20) : rankings;

        return ApiResponse.success(top20);
    }
}