package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawmatch.entity.FosterServiceEntity;
import com.pawmatch.entity.FosterOrder;
import com.pawmatch.entity.Shelter;
import com.pawmatch.entity.Notification;
import com.pawmatch.mapper.FosterServiceMapper;
import com.pawmatch.mapper.FosterOrderMapper;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.NotificationMapper;
import com.pawmatch.dto.request.AddFosterServiceRequest;
import com.pawmatch.dto.request.UpdateFosterServiceRequest;
import com.pawmatch.dto.request.FosterServiceSearchRequest;
import com.pawmatch.dto.request.CreateFosterOrderRequest;
import com.pawmatch.dto.request.OrderQueryRequest;
import com.pawmatch.dto.request.ReviewRequest;
import com.pawmatch.dto.response.FosterServiceResponse;
import com.pawmatch.dto.response.FosterServiceDetailResponse;
import com.pawmatch.dto.response.FosterOrderResponse;
import com.pawmatch.dto.response.ShelterResponse;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.exception.ErrorCode;
import com.pawmatch.service.MessagePushService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FosterServiceImpl extends ServiceImpl<FosterServiceMapper, FosterServiceEntity> implements com.pawmatch.service.FosterService {

    private final FosterOrderMapper fosterOrderMapper;
    private final ShelterMapper shelterMapper;
    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;
    private final GrowthServiceImpl growthService;
    private final MessagePushService messagePushService;

    public FosterServiceImpl(FosterOrderMapper fosterOrderMapper, ShelterMapper shelterMapper,
                             NotificationMapper notificationMapper, ObjectMapper objectMapper,
                             GrowthServiceImpl growthService, MessagePushService messagePushService) {
        this.fosterOrderMapper = fosterOrderMapper;
        this.shelterMapper = shelterMapper;
        this.notificationMapper = notificationMapper;
        this.objectMapper = objectMapper;
        this.growthService = growthService;
        this.messagePushService = messagePushService;
    }

    @Override
    @Transactional
    public Long addFosterService(AddFosterServiceRequest request, Long shelterId) {
        FosterServiceEntity fs = new FosterServiceEntity();
        fs.setShelterId(shelterId);
        fs.setTitle(request.getTitle());
        fs.setDescription(request.getDescription());
        fs.setPetType(request.getPetType());
        fs.setPricePerDay(request.getPricePerDay());
        fs.setMaxCapacity(request.getMaxCapacity());
        fs.setStatus(1);
        fs.setCreateTime(LocalDateTime.now());
        fs.setUpdateTime(LocalDateTime.now());
        try {
            if (request.getAvailableDates() != null) {
                fs.setAvailableDates(objectMapper.writeValueAsString(request.getAvailableDates()));
            }
            if (request.getImages() != null) {
                fs.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "JSON序列化失败");
        }
        save(fs);
        return fs.getId();
    }

    @Override
    @Transactional
    public void updateFosterService(UpdateFosterServiceRequest request, Long serviceId) {
        FosterServiceEntity fs = getById(serviceId);
        if (fs == null) {
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }
        if (request.getTitle() != null) fs.setTitle(request.getTitle());
        if (request.getDescription() != null) fs.setDescription(request.getDescription());
        if (request.getPetType() != null) fs.setPetType(request.getPetType());
        if (request.getPricePerDay() != null) fs.setPricePerDay(request.getPricePerDay());
        if (request.getMaxCapacity() != null) fs.setMaxCapacity(request.getMaxCapacity());
        try {
            if (request.getAvailableDates() != null) {
                fs.setAvailableDates(objectMapper.writeValueAsString(request.getAvailableDates()));
            }
            if (request.getImages() != null) {
                fs.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "JSON序列化失败");
        }
        fs.setUpdateTime(LocalDateTime.now());
        updateById(fs);
    }

    @Override
    @Transactional
    public void deleteFosterService(Long serviceId, Long shelterId) {
        FosterServiceEntity fs = getById(serviceId);
        if (fs == null || !fs.getShelterId().equals(shelterId)) {
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }
        fs.setStatus(0);
        fs.setUpdateTime(LocalDateTime.now());
        updateById(fs);
    }

    @Override
    public IPage<FosterServiceResponse> searchFosterServices(FosterServiceSearchRequest request) {
        Page<FosterServiceEntity> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<FosterServiceEntity> wrapper = Wrappers.lambdaQuery(FosterServiceEntity.class)
                .eq(FosterServiceEntity::getStatus, 1)
                .eq(request.getPetType() != null, FosterServiceEntity::getPetType, request.getPetType())
                .ge(request.getMinPrice() != null, FosterServiceEntity::getPricePerDay, request.getMinPrice())
                .le(request.getMaxPrice() != null, FosterServiceEntity::getPricePerDay, request.getMaxPrice());
        IPage<FosterServiceEntity> result = page(page, wrapper);
        IPage<FosterServiceResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(fs -> {
            FosterServiceResponse r = new FosterServiceResponse();
            BeanUtils.copyProperties(fs, r);
            r.setCreateTime(fs.getCreateTime());
            Shelter shelter = shelterMapper.selectById(fs.getShelterId());
            if (shelter != null) {
                r.setShelterName(shelter.getNickname());
            }
            return r;
        }).collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    public FosterServiceDetailResponse getFosterServiceDetail(Long serviceId) {
        FosterServiceEntity fs = getById(serviceId);
        if (fs == null) {
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }
        FosterServiceDetailResponse detail = new FosterServiceDetailResponse();
        BeanUtils.copyProperties(fs, detail);
        detail.setCreateTime(fs.getCreateTime());
        Shelter shelter = shelterMapper.selectById(fs.getShelterId());
        if (shelter != null) {
            ShelterResponse sr = new ShelterResponse();
            BeanUtils.copyProperties(shelter, sr);
            sr.setCreateTime(shelter.getCreateTime());
            detail.setShelter(sr);
        }
        return detail;
    }

    @Override
    @Transactional
    public Long createFosterOrder(CreateFosterOrderRequest request, Long userId) {
        FosterServiceEntity fs = getById(request.getServiceId());
        if (fs == null || fs.getStatus() != 1) {
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(request.getStartDate(), fmt);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), fmt);
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) {
            throw new BusinessException(400, "结束日期必须晚于开始日期");
        }
        int totalDays = (int) days;
        double totalPrice = totalDays * fs.getPricePerDay();

        FosterOrder order = new FosterOrder();
        order.setServiceId(request.getServiceId());
        order.setUserId(userId);
        order.setShelterId(fs.getShelterId());
        order.setPetName(request.getPetName());
        order.setPetType(request.getPetType());
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setTotalDays(totalDays);
        order.setTotalPrice(totalPrice);
        order.setSpecialRequests(request.getSpecialRequests());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        fosterOrderMapper.insert(order);

        Notification notification = new Notification();
        notification.setUserId(fs.getShelterId());
        notification.setUserType(1);
        notification.setType("foster_order");
        notification.setTitle("新寄养订单");
        notification.setContent("您收到一个新的寄养预约");
        notification.setRelatedId(order.getId());
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        // 积分：提交寄养申请+5
        growthService.awardPoints(userId, 5, "FOSTER_APPLY", "提交寄养申请");

        // WebSocket 推送新寄养订单给救助站
        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("orderId", order.getId());
            pushData.put("serviceId", request.getServiceId());
            pushData.put("petName", request.getPetName());
            pushData.put("startDate", request.getStartDate());
            pushData.put("endDate", request.getEndDate());
            pushData.put("status", 0);
            pushData.put("userId", userId);
            messagePushService.pushFosterOrderCreated(fs.getShelterId(), pushData);
        } catch (Exception ignored) {}

        return order.getId();
    }

    @Override
    public IPage<FosterOrderResponse> getOrderList(OrderQueryRequest request, Long shelterId) {
        Page<FosterOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<FosterOrder> wrapper = Wrappers.lambdaQuery(FosterOrder.class)
                .eq(FosterOrder::getShelterId, shelterId)
                .eq(request.getStatus() != null, FosterOrder::getStatus, request.getStatus());
        IPage<FosterOrder> result = fosterOrderMapper.selectPage(page, wrapper);
        return convertOrderPage(result);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long shelterId) {
        FosterOrder order = fosterOrderMapper.selectById(orderId);
        if (order == null || !order.getShelterId().equals(shelterId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        order.setStatus(1);
        fosterOrderMapper.updateById(order);

        // WebSocket 推送状态变更给用户
        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("orderId", orderId);
            pushData.put("status", 1);
            pushData.put("action", "confirmed");
            messagePushService.pushFosterOrderUpdated(order.getUserId(), pushData);
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long shelterId) {
        FosterOrder order = fosterOrderMapper.selectById(orderId);
        if (order == null || !order.getShelterId().equals(shelterId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        order.setStatus(3);
        fosterOrderMapper.updateById(order);

        // WebSocket 推送完成通知给用户
        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("orderId", orderId);
            pushData.put("status", 3);
            pushData.put("action", "completed");
            messagePushService.pushFosterOrderUpdated(order.getUserId(), pushData);
        } catch (Exception ignored) {}
    }

    @Override
    public IPage<FosterOrderResponse> getMyOrders(Long userId, Integer pageNum, Integer pageSize) {
        Page<FosterOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FosterOrder> wrapper = Wrappers.lambdaQuery(FosterOrder.class)
                .eq(FosterOrder::getUserId, userId);
        IPage<FosterOrder> result = fosterOrderMapper.selectPage(page, wrapper);
        return convertOrderPage(result);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        FosterOrder order = fosterOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(4);
        fosterOrderMapper.updateById(order);

        // WebSocket 推送取消通知给救助站
        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("orderId", orderId);
            pushData.put("status", 4);
            pushData.put("action", "cancelled");
            messagePushService.pushFosterOrderUpdated(order.getShelterId(), pushData);
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void reviewOrder(ReviewRequest request, Long orderId, Long userId) {
        FosterOrder order = fosterOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 3) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }
        order.setRating(request.getRating());
        order.setComment(request.getComment());
        fosterOrderMapper.updateById(order);
    }

    private IPage<FosterOrderResponse> convertOrderPage(IPage<FosterOrder> result) {
        IPage<FosterOrderResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(o -> {
            FosterOrderResponse r = new FosterOrderResponse();
            BeanUtils.copyProperties(o, r);
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            r.setStartDate(o.getStartDate() != null ? o.getStartDate().format(dateFmt) : null);
            r.setEndDate(o.getEndDate() != null ? o.getEndDate().format(dateFmt) : null);
            r.setCreateTime(o.getCreateTime());
            Shelter shelter = shelterMapper.selectById(o.getShelterId());
            if (shelter != null) {
                r.setShelterName(shelter.getNickname());
            }
            FosterServiceEntity fs = getById(o.getServiceId());
            if (fs != null) {
                r.setServiceName(fs.getTitle());
            }
            return r;
        }).collect(Collectors.toList()));
        return responsePage;
    }
}
