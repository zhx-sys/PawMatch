package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.CreditLog;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.impl.CreditService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credit")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping("/logs")
    public ApiResponse<IPage<CreditLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        IPage<CreditLog> page = creditService.getCreditLogs(
                principal.getUserId(), principal.getUserType(), pageNum, pageSize);
        return ApiResponse.success(page);
    }
}