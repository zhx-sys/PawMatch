package com.pawmatch.controller;

import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.ReportResponse;
import com.pawmatch.entity.Report;
import com.pawmatch.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ApiResponse<Report> create(@RequestBody Report report) {
        return ApiResponse.success(reportService.create(report));
    }

    @GetMapping("/pending")
    public ApiResponse<List<ReportResponse>> getPending() {
        return ApiResponse.success(reportService.getPending());
    }

    @PutMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        reportService.review(id, status);
        return ApiResponse.success(null);
    }
}
