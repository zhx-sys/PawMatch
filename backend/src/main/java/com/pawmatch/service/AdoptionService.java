package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.AdoptionApplication;
import com.pawmatch.dto.request.AdoptionApplicationRequest;
import com.pawmatch.dto.request.AuditRequest;
import com.pawmatch.dto.request.ApplicationQueryRequest;
import com.pawmatch.dto.response.ApplicationResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface AdoptionService extends IService<AdoptionApplication> {
    Long submitApplication(AdoptionApplicationRequest request, Long userId);
    IPage<ApplicationResponse> getApplicationList(ApplicationQueryRequest request, Long shelterId);
    void auditApplication(AuditRequest request, Long applicationId);
    void completeAdoption(Long applicationId, Long shelterId);
    IPage<ApplicationResponse> getMyApplications(Long userId, Integer pageNum, Integer pageSize);
    void cancelApplication(Long applicationId, Long userId);
}
