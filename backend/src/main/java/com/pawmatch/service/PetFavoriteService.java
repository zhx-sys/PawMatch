package com.pawmatch.service;

public interface PetFavoriteService {
    boolean toggleFavorite(Long userId, Long petId);
    boolean isFavorited(Long userId, Long petId);
    java.util.List<Long> getFavoritePetIds(Long userId);
    java.util.List<com.pawmatch.entity.Pet> getFavoritePets(Long userId);
}
