package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pawmatch.entity.Pet;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.PetMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.dto.response.MatchedPetResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final UserMapper userMapper;
    private final PetMapper petMapper;

    public MatchingService(UserMapper userMapper, PetMapper petMapper) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
    }

    public List<MatchedPetResponse> recommend(Long userId, int limit) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Pet> wrapper = Wrappers.lambdaQuery(Pet.class)
                .eq(Pet::getStatus, 0);
        List<Pet> availablePets = petMapper.selectList(wrapper);

        if (availablePets.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchedPetResponse> results = new ArrayList<>();
        for (Pet pet : availablePets) {
            MatchResult mr = calculateMatch(user, pet);
            MatchedPetResponse response = buildResponse(pet, mr);
            results.add(response);
        }

        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        if (limit > 0 && results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }

    private MatchResult calculateMatch(User user, Pet pet) {
        Map<String, Integer> details = new LinkedHashMap<>();
        double totalScore = 0;

        // 1. 空间匹配 (20%)
        int spaceScore = calcSpaceScore(user.getLivingSpace(), pet.getSizeLevel());
        details.put("空间匹配", spaceScore);
        totalScore += spaceScore * 0.20;

        // 2. 经验匹配 (15%)
        int expScore = calcExperienceScore(user.getPetExperience(), pet.getBeginnerFriendly());
        details.put("经验匹配", expScore);
        totalScore += expScore * 0.15;

        // 3. 儿童匹配 (15%)
        int kidsScore = calcKidsScore(user.getHasChildren(), pet.getGoodWithKids());
        details.put("儿童匹配", kidsScore);
        totalScore += kidsScore * 0.15;

        // 4. 宠物匹配 (15%)
        int petScore = calcPetScore(user.getHasOtherPets(), pet.getGoodWithPets());
        details.put("宠物匹配", petScore);
        totalScore += petScore * 0.15;

        // 5. 作息匹配 (10%)
        int routineScore = calcRoutineScore(user.getDailyRoutine(), pet.getActivityLevel());
        details.put("作息匹配", routineScore);
        totalScore += routineScore * 0.10;

        // 6. 预算匹配 (5%)
        int budgetScore = calcBudgetScore(user.getBudgetRange(), pet.getType(), pet.getSizeLevel());
        details.put("预算匹配", budgetScore);
        totalScore += budgetScore * 0.05;

        // 7. 偏好加分 (20%)
        int prefScore = calcPreferenceScore(user.getPetPreference(), pet.getType(), pet.getBreed());
        details.put("偏好加分", prefScore);
        totalScore += prefScore * 0.20;

        int finalScore = (int) Math.round(Math.max(0, Math.min(100, totalScore)));
        return new MatchResult(finalScore, details);
    }

    private int calcSpaceScore(String livingSpace, String sizeLevel) {
        if (livingSpace == null || sizeLevel == null) return 50;
        if ("别墅".equals(livingSpace) && "大型".equals(sizeLevel)) return 100;
        if ("别墅".equals(livingSpace)) return 90;
        if ("大户型".equals(livingSpace) && "大型".equals(sizeLevel)) return 80;
        if ("大户型".equals(livingSpace)) return 85;
        if ("普通住宅".equals(livingSpace) && "大型".equals(sizeLevel)) return 50;
        if ("普通住宅".equals(livingSpace)) return 70;
        if ("公寓".equals(livingSpace) && "大型".equals(sizeLevel)) return 20;
        if ("公寓".equals(livingSpace) && "中型".equals(sizeLevel)) return 50;
        if ("公寓".equals(livingSpace)) return 60;
        return 50;
    }

    private int calcExperienceScore(String petExperience, Boolean beginnerFriendly) {
        if (petExperience == null) return 50;
        if ("新手".equals(petExperience)) {
            return Boolean.TRUE.equals(beginnerFriendly) ? 100 : 20;
        }
        if ("有经验".equals(petExperience)) return 80;
        if ("资深".equals(petExperience)) return 90;
        return 50;
    }

    private int calcKidsScore(Boolean hasChildren, Boolean goodWithKids) {
        if (!Boolean.TRUE.equals(hasChildren)) return 100;
        return Boolean.TRUE.equals(goodWithKids) ? 100 : 25;
    }

    private int calcPetScore(Boolean hasOtherPets, Boolean goodWithPets) {
        if (!Boolean.TRUE.equals(hasOtherPets)) return 100;
        return Boolean.TRUE.equals(goodWithPets) ? 100 : 25;
    }

    private int calcRoutineScore(String dailyRoutine, String activityLevel) {
        if (dailyRoutine == null || activityLevel == null) return 50;

        // 新值：活泼好动/温顺安静/粘人精/独立自主
        // 旧值兼容：高/中/低
        boolean isActive = "活泼好动".equals(activityLevel) || "高".equals(activityLevel);
        boolean isCalm = "温顺安静".equals(activityLevel) || "低".equals(activityLevel);
        boolean isClingy = "粘人精".equals(activityLevel);
        boolean isIndependent = "独立自主".equals(activityLevel);

        if ("朝九晚五".equals(dailyRoutine)) {
            if (isActive) return 20;
            if (isClingy) return 35;
            if (isIndependent) return 85;
            if (isCalm) return 80;
            return 60; // 中 / 其他
        }
        if ("自由职业".equals(dailyRoutine)) return 85;
        if ("居家".equals(dailyRoutine)) {
            if (isClingy) return 100; // 粘人精最适合居家
            return 90;
        }
        return 50;
    }

    private int calcBudgetScore(String budgetRange, String type, String sizeLevel) {
        if (budgetRange == null) return 50;
        boolean isLargeDog = "狗".equals(type) && "大型".equals(sizeLevel);
        if ("高（>800/月）".equals(budgetRange)) return 100;
        if ("中（300-800/月）".equals(budgetRange)) {
            return isLargeDog ? 50 : 80;
        }
        if ("低（<300/月）".equals(budgetRange)) {
            return isLargeDog ? 20 : isLargeDog ? 20 : 60;
        }
        return 50;
    }

    private int calcPreferenceScore(String petPreference, String petType, String breed) {
        if (petPreference == null || petPreference.trim().isEmpty()) return 30;
        String[] keywords = petPreference.split("[,，\\s]+");
        int score = 0;
        for (String kw : keywords) {
            String k = kw.trim().toLowerCase();
            if (k.isEmpty()) continue;
            if (k.equals(petType != null ? petType.toLowerCase() : "")) score += 5;
            else if (breed != null && breed.toLowerCase().contains(k)) score += 5;
        }
        return Math.min(score, 15);
    }

    private MatchedPetResponse buildResponse(Pet pet, MatchResult mr) {
        MatchedPetResponse r = new MatchedPetResponse();
        r.setId(pet.getId());
        r.setName(pet.getName());
        r.setType(pet.getType());
        r.setBreed(pet.getBreed());
        r.setGender(pet.getGender());
        r.setAge(pet.getAge());
        r.setColor(pet.getColor());
        r.setHealthStatus(pet.getHealthStatus());
        r.setVaccinated(pet.getVaccinated());
        r.setSterilized(pet.getSterilized());
        r.setImages(pet.getImages());
        r.setStatus(pet.getStatus());
        r.setShelterId(pet.getShelterId());
        r.setCreateTime(pet.getCreateTime());
        r.setMatchScore(mr.score);
        r.setMatchDetails(mr.details);
        return r;
    }

    public Map<String, Object> getQuestionnaire() {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("livingSpace", Map.of(
            "label", "您的居住空间",
            "options", List.of(
                Map.of("value", "公寓", "label", "公寓"),
                Map.of("value", "普通住宅", "label", "普通住宅"),
                Map.of("value", "大户型", "label", "大户型"),
                Map.of("value", "别墅", "label", "别墅")
        )));
        q.put("hasChildren", Map.of(
            "label", "家中是否有儿童",
            "options", List.of(
                Map.of("value", false, "label", "无"),
                Map.of("value", true, "label", "有")
        )));
        q.put("hasOtherPets", Map.of(
            "label", "是否已有其他宠物",
            "options", List.of(
                Map.of("value", false, "label", "无"),
                Map.of("value", true, "label", "有")
        )));
        q.put("petExperience", Map.of(
            "label", "养宠经验",
            "options", List.of(
                Map.of("value", "新手", "label", "新手"),
                Map.of("value", "有经验", "label", "有经验"),
                Map.of("value", "资深", "label", "资深")
        )));
        q.put("dailyRoutine", Map.of(
            "label", "作息规律",
            "options", List.of(
                Map.of("value", "朝九晚五", "label", "朝九晚五"),
                Map.of("value", "自由职业", "label", "自由职业"),
                Map.of("value", "居家", "label", "居家")
        )));
        q.put("budgetRange", Map.of(
            "label", "月度预算",
            "options", List.of(
                Map.of("value", "低（<300/月）", "label", "低（<300/月）"),
                Map.of("value", "中（300-800/月）", "label", "中（300-800/月）"),
                Map.of("value", "高（>800/月）", "label", "高（>800/月）")
        )));
        q.put("petPreference", Map.of(
            "label", "宠物偏好（可选）",
            "placeholder", "如：猫 金毛 小型犬（多个关键词空格分隔）"
        ));
        return q;
    }

    private static class MatchResult {
        int score;
        Map<String, Integer> details;
        MatchResult(int score, Map<String, Integer> details) {
            this.score = score;
            this.details = details;
        }
    }
}