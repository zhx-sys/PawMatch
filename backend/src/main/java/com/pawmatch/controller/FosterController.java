package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.request.*;
import com.pawmatch.dto.response.*;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.FosterService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foster")
public class FosterController {

    private final FosterService fosterService;

    public FosterController(FosterService fosterService) {
        this.fosterService = fosterService;
    }

    @PostMapping("/service")
    public ApiResponse<Long> addFosterService(@Valid @RequestBody AddFosterServiceRequest request) {
        Long shelterId = getCurrentUserId();
        Long id = fosterService.addFosterService(request, shelterId);
        return ApiResponse.success("发布成功", id);
    }

    @PutMapping("/service/{serviceId}")
    public ApiResponse<Void> updateFosterService(@PathVariable Long serviceId,
                                                  @Valid @RequestBody UpdateFosterServiceRequest request) {
        fosterService.updateFosterService(request, serviceId);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/service/{serviceId}")
    public ApiResponse<Void> deleteFosterService(@PathVariable Long serviceId) {
        Long shelterId = getCurrentUserId();
        fosterService.deleteFosterService(serviceId, shelterId);
        return ApiResponse.success("下架成功", null);
    }

    @GetMapping("/service/search")
    public ApiResponse<IPage<FosterServiceResponse>> searchFosterServices(
            @ModelAttribute FosterServiceSearchRequest request) {
        IPage<FosterServiceResponse> page = fosterService.searchFosterServices(request);
        return ApiResponse.success(page);
    }

    @GetMapping("/service/{serviceId}")
    public ApiResponse<FosterServiceDetailResponse> getFosterServiceDetail(@PathVariable Long serviceId) {
        FosterServiceDetailResponse detail = fosterService.getFosterServiceDetail(serviceId);
        return ApiResponse.success(detail);
    }

    @PostMapping("/order")
    public ApiResponse<Long> createFosterOrder(@Valid @RequestBody CreateFosterOrderRequest request) {
        Long userId = getCurrentUserId();
        Long id = fosterService.createFosterOrder(request, userId);
        return ApiResponse.success("预约成功", id);
    }

    @GetMapping("/order/list")
    public ApiResponse<IPage<FosterOrderResponse>> getOrderList(@ModelAttribute OrderQueryRequest request) {
        Long shelterId = getCurrentUserId();
        IPage<FosterOrderResponse> page = fosterService.getOrderList(request, shelterId);
        return ApiResponse.success(page);
    }

    @PutMapping("/order/{orderId}/confirm")
    public ApiResponse<Void> confirmOrder(@PathVariable Long orderId) {
        Long shelterId = getCurrentUserId();
        fosterService.confirmOrder(orderId, shelterId);
        return ApiResponse.success("确认成功", null);
    }

    @PutMapping("/order/{orderId}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long orderId) {
        Long shelterId = getCurrentUserId();
        fosterService.completeOrder(orderId, shelterId);
        return ApiResponse.success("完成成功", null);
    }

    @GetMapping("/order/my")
    public ApiResponse<IPage<FosterOrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        IPage<FosterOrderResponse> page = fosterService.getMyOrders(userId, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @PutMapping("/order/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        fosterService.cancelOrder(orderId, userId);
        return ApiResponse.success("取消成功", null);
    }

    @PutMapping("/order/{orderId}/review")
    public ApiResponse<Void> reviewOrder(@PathVariable Long orderId,
                                          @Valid @RequestBody ReviewRequest request) {
        Long userId = getCurrentUserId();
        fosterService.reviewOrder(request, orderId, userId);
        return ApiResponse.success("评价成功", null);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
