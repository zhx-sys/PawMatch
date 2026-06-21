package com.pawmatch.controller;

import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.Pet;
import com.pawmatch.service.PetFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pet-favorite")
public class PetFavoriteController {

    @Autowired
    private PetFavoriteService petFavoriteService;

    @PostMapping("/toggle")
    public ApiResponse<Map<String, Boolean>> toggle(@RequestParam Long userId, @RequestParam Long petId) {
        boolean favorited = petFavoriteService.toggleFavorite(userId, petId);
        return ApiResponse.success(Map.of("favorited", favorited));
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Boolean>> status(@RequestParam Long userId, @RequestParam Long petId) {
        return ApiResponse.success(Map.of("favorited", petFavoriteService.isFavorited(userId, petId)));
    }

    @GetMapping("/list")
    public ApiResponse<List<Pet>> list(@RequestParam Long userId) {
        return ApiResponse.success(petFavoriteService.getFavoritePets(userId));
    }

    @GetMapping("/ids")
    public ApiResponse<List<Long>> ids(@RequestParam Long userId) {
        return ApiResponse.success(petFavoriteService.getFavoritePetIds(userId));
    }
}
