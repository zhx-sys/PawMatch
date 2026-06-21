package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.FosterServiceEntity;
import com.pawmatch.dto.request.AddFosterServiceRequest;
import com.pawmatch.dto.request.UpdateFosterServiceRequest;
import com.pawmatch.dto.request.FosterServiceSearchRequest;
import com.pawmatch.dto.request.CreateFosterOrderRequest;
import com.pawmatch.dto.request.OrderQueryRequest;
import com.pawmatch.dto.request.ReviewRequest;
import com.pawmatch.dto.response.FosterServiceResponse;
import com.pawmatch.dto.response.FosterServiceDetailResponse;
import com.pawmatch.dto.response.FosterOrderResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface FosterService extends IService<FosterServiceEntity> {
    Long addFosterService(AddFosterServiceRequest request, Long shelterId);
    void updateFosterService(UpdateFosterServiceRequest request, Long serviceId);
    void deleteFosterService(Long serviceId, Long shelterId);
    IPage<FosterServiceResponse> searchFosterServices(FosterServiceSearchRequest request);
    FosterServiceDetailResponse getFosterServiceDetail(Long serviceId);
    Long createFosterOrder(CreateFosterOrderRequest request, Long userId);
    IPage<FosterOrderResponse> getOrderList(OrderQueryRequest request, Long shelterId);
    void confirmOrder(Long orderId, Long shelterId);
    void completeOrder(Long orderId, Long shelterId);
    IPage<FosterOrderResponse> getMyOrders(Long userId, Integer pageNum, Integer pageSize);
    void cancelOrder(Long orderId, Long userId);
    void reviewOrder(ReviewRequest request, Long orderId, Long userId);
}
