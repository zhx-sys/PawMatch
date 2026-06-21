package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawmatch.entity.CreditLog;
import com.pawmatch.entity.User;
import com.pawmatch.entity.Shelter;
import com.pawmatch.mapper.CreditLogMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.AdoptionApplicationMapper;
import com.pawmatch.entity.AdoptionApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreditService {

    private final CreditLogMapper creditLogMapper;
    private final UserMapper userMapper;
    private final ShelterMapper shelterMapper;
    private final AdoptionApplicationMapper adoptionApplicationMapper;

    public CreditService(CreditLogMapper creditLogMapper, UserMapper userMapper,
                         ShelterMapper shelterMapper, AdoptionApplicationMapper adoptionApplicationMapper) {
        this.creditLogMapper = creditLogMapper;
        this.userMapper = userMapper;
        this.shelterMapper = shelterMapper;
        this.adoptionApplicationMapper = adoptionApplicationMapper;
    }

    @Transactional
    public void changeCredit(Long userId, Integer userType, int scoreChange,
                             String reasonType, String reasonDetail, Long relatedId) {
        int currentScore;
        if (userType == 1) {
            Shelter shelter = shelterMapper.selectById(userId);
            if (shelter == null) return;
            currentScore = shelter.getCreditScore() != null ? shelter.getCreditScore() : 100;
            int newScore = Math.max(0, Math.min(100, currentScore + scoreChange));
            shelter.setCreditScore(newScore);
            shelterMapper.updateById(shelter);
        } else {
            User user = userMapper.selectById(userId);
            if (user == null) return;
            currentScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
            int newScore = Math.max(0, Math.min(100, currentScore + scoreChange));
            user.setCreditScore(newScore);
            userMapper.updateById(user);
        }

        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setUserType(userType);
        log.setScoreChange(scoreChange);
        log.setScoreAfter(Math.max(0, Math.min(100, currentScore + scoreChange)));
        log.setReasonType(reasonType);
        log.setReasonDetail(reasonDetail);
        log.setRelatedId(relatedId);
        log.setCreateTime(LocalDateTime.now());
        creditLogMapper.insert(log);
    }

    @Transactional
    public void checkFloodApply(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<AdoptionApplication> wrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getUserId, userId)
                .ge(AdoptionApplication::getApplyTime, since);
        long count = adoptionApplicationMapper.selectCount(wrapper);
        if (count > 3) {
            changeCredit(userId, 0, -10, "FLOOD_APPLY",
                    "24小时内申请超过3次，触发刷申请扣分", null);
        }
    }

    public IPage<CreditLog> getCreditLogs(Long userId, Integer userType, int pageNum, int pageSize) {
        Page<CreditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CreditLog> wrapper = Wrappers.lambdaQuery(CreditLog.class)
                .eq(CreditLog::getUserId, userId)
                .eq(CreditLog::getUserType, userType)
                .orderByDesc(CreditLog::getCreateTime);
        return creditLogMapper.selectPage(page, wrapper);
    }
}