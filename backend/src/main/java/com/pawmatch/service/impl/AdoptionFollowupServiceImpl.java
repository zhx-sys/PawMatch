package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawmatch.entity.AdoptionApplication;
import com.pawmatch.entity.AdoptionFollowup;
import com.pawmatch.mapper.AdoptionApplicationMapper;
import com.pawmatch.mapper.AdoptionFollowupMapper;
import com.pawmatch.service.AdoptionFollowupService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdoptionFollowupServiceImpl implements AdoptionFollowupService {

    private final AdoptionFollowupMapper followupMapper;
    private final AdoptionApplicationMapper adoptionMapper;
    private final CreditService creditService;

    public AdoptionFollowupServiceImpl(AdoptionFollowupMapper followupMapper,
                                       AdoptionApplicationMapper adoptionMapper,
                                       CreditService creditService) {
        this.followupMapper = followupMapper;
        this.adoptionMapper = adoptionMapper;
        this.creditService = creditService;
    }

    @Override
    public AdoptionFollowup create(AdoptionFollowup followup) {
        AdoptionApplication app = null;
        if (followup.getShelterId() == null || followup.getShelterId() == 0) {
            app = adoptionMapper.selectById(followup.getAdoptionId());
            if (app != null) {
                followup.setShelterId(app.getShelterId());
            }
        }
        followup.setCreateTime(LocalDateTime.now());
        followupMapper.insert(followup);

        // 确保拿到领养申请信息用于信用加分
        if (app == null) {
            app = adoptionMapper.selectById(followup.getAdoptionId());
        }
        if (app != null) {
            creditService.changeCredit(app.getUserId(), 0, 3, "RETURN_VISIT",
                    "完成回访", followup.getAdoptionId());
        }

        return followup;
    }

    @Override
    public List<AdoptionFollowup> getByAdoptionId(Long adoptionId) {
        LambdaQueryWrapper<AdoptionFollowup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionFollowup::getAdoptionId, adoptionId).orderByDesc(AdoptionFollowup::getCreateTime);
        return followupMapper.selectList(wrapper);
    }

    @Override
    public List<AdoptionFollowup> getByShelterId(Long shelterId) {
        return followupMapper.selectByShelterIdWithJoin(shelterId);
    }
}
