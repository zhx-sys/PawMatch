package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawmatch.entity.Pet;
import com.pawmatch.entity.PetFavorite;
import com.pawmatch.mapper.PetFavoriteMapper;
import com.pawmatch.mapper.PetMapper;
import com.pawmatch.service.PetFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetFavoriteServiceImpl implements PetFavoriteService {

    @Autowired
    private PetFavoriteMapper petFavoriteMapper;

    @Autowired
    private PetMapper petMapper;

    @Override
    public boolean toggleFavorite(Long userId, Long petId) {
        LambdaQueryWrapper<PetFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetFavorite::getUserId, userId).eq(PetFavorite::getPetId, petId);
        PetFavorite existing = petFavoriteMapper.selectOne(wrapper);
        if (existing != null) {
            petFavoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            PetFavorite fav = new PetFavorite();
            fav.setUserId(userId);
            fav.setPetId(petId);
            fav.setCreateTime(LocalDateTime.now());
            petFavoriteMapper.insert(fav);
            return true;
        }
    }

    @Override
    public boolean isFavorited(Long userId, Long petId) {
        LambdaQueryWrapper<PetFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetFavorite::getUserId, userId).eq(PetFavorite::getPetId, petId);
        return petFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Long> getFavoritePetIds(Long userId) {
        LambdaQueryWrapper<PetFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetFavorite::getUserId, userId);
        return petFavoriteMapper.selectList(wrapper).stream()
                .map(PetFavorite::getPetId).collect(Collectors.toList());
    }

    @Override
    public List<Pet> getFavoritePets(Long userId) {
        List<Long> petIds = getFavoritePetIds(userId);
        if (petIds.isEmpty()) return List.of();
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Pet::getId, petIds);
        return petMapper.selectList(wrapper);
    }
}
