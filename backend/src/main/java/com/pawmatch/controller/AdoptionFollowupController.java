package com.pawmatch.controller;

import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.AdoptionFollowup;
import com.pawmatch.service.AdoptionFollowupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/followup")
public class AdoptionFollowupController {

    @Autowired
    private AdoptionFollowupService followupService;

    @PostMapping
    public ApiResponse<AdoptionFollowup> create(@RequestBody AdoptionFollowup followup) {
        return ApiResponse.success(followupService.create(followup));
    }

    @GetMapping("/adoption/{adoptionId}")
    public ApiResponse<List<AdoptionFollowup>> getByAdoption(@PathVariable Long adoptionId) {
        return ApiResponse.success(followupService.getByAdoptionId(adoptionId));
    }

    @GetMapping("/shelter/{shelterId}")
    public ApiResponse<List<AdoptionFollowup>> getByShelter(@PathVariable Long shelterId) {
        return ApiResponse.success(followupService.getByShelterId(shelterId));
    }
}
