package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pawmatch.entity.AdoptionApplication;
import com.pawmatch.entity.Pet;
import com.pawmatch.entity.Notification;
import com.pawmatch.mapper.AdoptionApplicationMapper;
import com.pawmatch.mapper.PetMapper;
import com.pawmatch.mapper.NotificationMapper;
import com.pawmatch.service.AdoptionService;
import com.pawmatch.service.MessagePushService;
import com.pawmatch.dto.request.AdoptionApplicationRequest;
import com.pawmatch.dto.request.AuditRequest;
import com.pawmatch.dto.request.ApplicationQueryRequest;
import com.pawmatch.dto.response.ApplicationResponse;
import com.pawmatch.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdoptionServiceImpl extends ServiceImpl<AdoptionApplicationMapper, AdoptionApplication> implements AdoptionService {

    private final PetMapper petMapper;
    private final NotificationMapper notificationMapper;
    private final CreditService creditService;
    private final GrowthServiceImpl growthService;
    private final MessagePushService messagePushService;

    public AdoptionServiceImpl(PetMapper petMapper, NotificationMapper notificationMapper,
                               CreditService creditService, GrowthServiceImpl growthService,
                               MessagePushService messagePushService) {
        this.petMapper = petMapper;
        this.notificationMapper = notificationMapper;
        this.creditService = creditService;
        this.growthService = growthService;
        this.messagePushService = messagePushService;
    }

    @Override
    @Transactional
    public Long submitApplication(AdoptionApplicationRequest request, Long userId) {
        LambdaQueryWrapper<AdoptionApplication> wrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getPetId, request.getPetId())
                .eq(AdoptionApplication::getUserId, userId)
                .in(AdoptionApplication::getStatus, 0, 1);
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "已存在待审核或已通过的申请");
        }
        // 刷申请检查：24h内申请次数>=3且用户未确认时拦截
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<AdoptionApplication> floodWrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getUserId, userId)
                .ge(AdoptionApplication::getApplyTime, since);
        long recentCount = count(floodWrapper);
        if (recentCount >= 3 && (request.getConfirmFlood() == null || !request.getConfirmFlood())) {
            throw new BusinessException(409, "您24小时内已提交超过3次领养申请，继续提交将扣除10信用分");
        }
        AdoptionApplication app = new AdoptionApplication();
        app.setPetId(request.getPetId());
        app.setUserId(userId);
        Pet pet = petMapper.selectById(request.getPetId());
        if (pet == null) {
            throw new BusinessException(404, "宠物不存在");
        }
        app.setShelterId(pet.getShelterId());
        app.setReason(request.getReason());
        app.setExperience(request.getExperience());
        app.setHousingCondition(request.getHousingCondition());
        app.setStatus(0);
        app.setApplyTime(LocalDateTime.now());
        save(app);
        creditService.checkFloodApply(userId);

        // 积分：提交领养申请+5
        growthService.awardPoints(userId, 5, "ADOPTION_APPLY", "提交领养申请");

        // WebSocket 推送新领养申请给救助站
        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("applicationId", app.getId());
            pushData.put("petId", app.getPetId());
            pushData.put("petName", pet.getName());
            pushData.put("applicantId", userId);
            pushData.put("status", 0);
            messagePushService.pushNewAdoption(app.getShelterId(), pushData);
        } catch (Exception ignored) {}

        return app.getId();
    }

    @Override
    public IPage<ApplicationResponse> getApplicationList(ApplicationQueryRequest request, Long shelterId) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AdoptionApplication> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<AdoptionApplication> wrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getShelterId, shelterId)
                .eq(request.getStatus() != null, AdoptionApplication::getStatus, request.getStatus())
                .eq(request.getPetId() != null, AdoptionApplication::getPetId, request.getPetId())
                .orderByDesc(AdoptionApplication::getApplyTime);
        IPage<AdoptionApplication> result = page(page, wrapper);
        IPage<ApplicationResponse> responsePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    @Transactional
    public void auditApplication(AuditRequest request, Long applicationId) {
        AdoptionApplication app = getById(applicationId);
        if (app == null) {
            throw new BusinessException(404, "申请不存在");
        }
        if (request.getStatus() == 1) {
            app.setStatus(1);
            app.setAuditTime(LocalDateTime.now());
        } else if (request.getStatus() == 2) {
            app.setStatus(2);
            app.setRejectReason(request.getRejectReason());
            app.setAuditTime(LocalDateTime.now());
            creditService.changeCredit(app.getUserId(), 0, -5, "APPLICATION_REJECTED",
                    "领养申请被拒绝", applicationId);
        } else {
            throw new BusinessException(400, "无效的审核状态");
        }
        updateById(app);
        Notification notification = new Notification();
        notification.setUserId(app.getUserId());
        notification.setUserType(0);
        notification.setType("adoption_audit");
        notification.setTitle(request.getStatus() == 1 ? "领养申请已通过" : "领养申请被拒绝");
        notification.setContent(request.getStatus() == 1 ? "您的领养申请已通过" : "您的领养申请被拒绝：" + request.getRejectReason());
        notification.setRelatedId(applicationId);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        // WebSocket 推送领养状态变更给申请人
        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("applicationId", applicationId);
            pushData.put("petId", app.getPetId());
            pushData.put("status", request.getStatus());
            pushData.put("rejectReason", request.getRejectReason());
            messagePushService.pushAdoptionStatusChange(app.getUserId(), pushData);
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void completeAdoption(Long applicationId, Long shelterId) {
        AdoptionApplication app = getById(applicationId);
        if (app == null || !app.getShelterId().equals(shelterId)) {
            throw new com.pawmatch.exception.BusinessException(404, "申请不存在");
        }
        app.setStatus(3);
        app.setCompleteTime(LocalDateTime.now());
        updateById(app);
        Pet pet = petMapper.selectById(app.getPetId());
        if (pet != null) {
            pet.setStatus(1);
            petMapper.updateById(pet);
        }
        creditService.changeCredit(app.getUserId(), 0, 10, "ADOPTION_COMPLETE",
                "领养完成", applicationId);
        // 成长激励：领养成功+20积分 + 检查徽章
        growthService.awardPoints(app.getUserId(), 20, "ADOPTION_COMPLETE", "领养成功");
        growthService.checkAndAwardBadges(app.getUserId(), 0);
    }

    @Override
    public IPage<ApplicationResponse> getMyApplications(Long userId, Integer pageNum, Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AdoptionApplication> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdoptionApplication> wrapper = Wrappers.lambdaQuery(AdoptionApplication.class)
                .eq(AdoptionApplication::getUserId, userId);
        IPage<AdoptionApplication> result = page(page, wrapper);
        IPage<ApplicationResponse> responsePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    @Transactional
    public void cancelApplication(Long applicationId, Long userId) {
        AdoptionApplication app = getById(applicationId);
        if (app == null || !app.getUserId().equals(userId)) {
            throw new BusinessException(404, "申请不存在");
        }
        if (app.getStatus() != 0) {
            throw new BusinessException(400, "只能撤销待审核的申请");
        }
        removeById(applicationId);
        creditService.changeCredit(userId, 0, -5, "APPLICATION_CANCELLED",
                "自行撤销领养申请", applicationId);
    }

    private ApplicationResponse toResponse(AdoptionApplication app) {
        ApplicationResponse r = new ApplicationResponse();
        BeanUtils.copyProperties(app, r);
        if (app.getPetId() != null) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                r.setPetName(pet.getName());
                r.setPetType(pet.getType());
            }
        }
        return r;
    }
}
