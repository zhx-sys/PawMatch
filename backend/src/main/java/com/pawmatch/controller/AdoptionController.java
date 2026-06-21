package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.request.AdoptionApplicationRequest;
import com.pawmatch.dto.request.ApplicationQueryRequest;
import com.pawmatch.dto.request.AuditRequest;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.ApplicationResponse;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.AdoptionService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adoption")
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @PostMapping("/apply")
    public ApiResponse<Long> submitApplication(@Valid @RequestBody AdoptionApplicationRequest request) {
        Long userId = getCurrentUserId();
        Long id = adoptionService.submitApplication(request, userId);
        return ApiResponse.success("申请提交成功", id);
    }

    @GetMapping("/list")
    public ApiResponse<IPage<ApplicationResponse>> getApplicationList(@ModelAttribute ApplicationQueryRequest request) {
        Long shelterId = getCurrentUserId();
        IPage<ApplicationResponse> page = adoptionService.getApplicationList(request, shelterId);
        return ApiResponse.success(page);
    }

    @PutMapping("/{applicationId}/audit")
    public ApiResponse<Void> auditApplication(@PathVariable Long applicationId,
                                               @Valid @RequestBody AuditRequest request) {
        adoptionService.auditApplication(request, applicationId);
        return ApiResponse.success("审核完成", null);
    }

    @PutMapping("/{applicationId}/complete")
    public ApiResponse<Void> completeAdoption(@PathVariable Long applicationId) {
        Long shelterId = getCurrentUserId();
        adoptionService.completeAdoption(applicationId, shelterId);
        return ApiResponse.success("领养完成", null);
    }

    @GetMapping("/my")
    public ApiResponse<IPage<ApplicationResponse>> getMyApplications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        IPage<ApplicationResponse> page = adoptionService.getMyApplications(userId, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @PutMapping("/{applicationId}/cancel")
    public ApiResponse<Void> cancelApplication(@PathVariable Long applicationId) {
        Long userId = getCurrentUserId();
        adoptionService.cancelApplication(applicationId, userId);
        return ApiResponse.success("撤销成功", null);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
