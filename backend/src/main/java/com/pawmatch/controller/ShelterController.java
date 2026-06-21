package com.pawmatch.controller;

import com.pawmatch.dto.request.UpdateShelterRequest;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.ShelterResponse;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.ShelterService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shelter")
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    @GetMapping("/info")
    public ApiResponse<ShelterResponse> getShelterInfo() {
        Long shelterId = getCurrentUserId();
        ShelterResponse response = shelterService.getShelterInfo(shelterId);
        return ApiResponse.success(response);
    }

    @PutMapping("/info")
    public ApiResponse<Void> updateShelterInfo(@Valid @RequestBody UpdateShelterRequest request) {
        Long shelterId = getCurrentUserId();
        shelterService.updateShelterInfo(request, shelterId);
        return ApiResponse.success("更新成功", null);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
