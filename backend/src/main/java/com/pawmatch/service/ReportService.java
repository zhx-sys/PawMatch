package com.pawmatch.service;

import com.pawmatch.dto.response.ReportResponse;
import com.pawmatch.entity.Report;
import java.util.List;

public interface ReportService {
    Report create(Report report);
    List<ReportResponse> getPending();
    void review(Long reportId, Integer status);
}
