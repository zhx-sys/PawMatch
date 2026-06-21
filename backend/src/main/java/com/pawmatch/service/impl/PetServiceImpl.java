package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pawmatch.entity.Pet;
import com.pawmatch.entity.Shelter;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.PetMapper;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.service.PetService;
import com.pawmatch.dto.request.AddPetRequest;
import com.pawmatch.dto.request.UpdatePetRequest;
import com.pawmatch.dto.request.PetQueryRequest;
import com.pawmatch.dto.request.PetSearchRequest;
import com.pawmatch.dto.response.PetResponse;
import com.pawmatch.dto.response.PetDetailResponse;
import com.pawmatch.dto.response.ShelterResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    private final ShelterMapper shelterMapper;

    private final UserMapper userMapper;

    public PetServiceImpl(ShelterMapper shelterMapper, UserMapper userMapper) {
        this.shelterMapper = shelterMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Long addPet(AddPetRequest request, Long shelterId) {
        Pet pet = new Pet();
        // Temporarily null images to avoid BeanUtils type mismatch (List<String> vs String)
        List<String> images = request.getImages();
        request.setImages(null);
        BeanUtils.copyProperties(request, pet);
        request.setImages(images);

        pet.setShelterId(shelterId);
        pet.setStatus(0); // 待领养
        if (images != null && !images.isEmpty()) {
            pet.setImages(String.join(",", images));
        }
        save(pet);
        return pet.getId();
    }

    @Override
    public void updatePet(UpdatePetRequest request, Long petId) {
        Pet pet = getById(petId);
        if (pet == null) {
            throw new com.pawmatch.exception.BusinessException(404, "宠物不存在");
        }
        if (request.getName() != null) pet.setName(request.getName());
        if (request.getType() != null) pet.setType(request.getType());
        if (request.getBreed() != null) pet.setBreed(request.getBreed());
        if (request.getGender() != null) pet.setGender(request.getGender());
        if (request.getAge() != null) pet.setAge(request.getAge());
        if (request.getColor() != null) pet.setColor(request.getColor());
        if (request.getWeight() != null) pet.setWeight(request.getWeight());
        if (request.getHealthStatus() != null) pet.setHealthStatus(request.getHealthStatus());
        if (request.getVaccinated() != null) pet.setVaccinated(request.getVaccinated());
        if (request.getSterilized() != null) pet.setSterilized(request.getSterilized());
        if (request.getDescription() != null) pet.setDescription(request.getDescription());
        if (request.getImages() != null) {
            pet.setImages(String.join(",", request.getImages()));
        }
        updateById(pet);
    }

    @Override
    public void deletePet(Long petId, Long shelterId) {
        Pet pet = getById(petId);
        if (pet == null || !pet.getShelterId().equals(shelterId)) {
            throw new com.pawmatch.exception.BusinessException(404, "宠物不存在");
        }
        pet.setStatus(2); // 已下架
        updateById(pet);
    }

    @Override
    public void restorePet(Long petId, Long shelterId) {
        Pet pet = getById(petId);
        if (pet == null || !pet.getShelterId().equals(shelterId)) {
            throw new com.pawmatch.exception.BusinessException(404, "宠物不存在");
        }
        if (pet.getStatus() != 2) {
            throw new com.pawmatch.exception.BusinessException(400, "该宠物未处于下架状态");
        }
        pet.setStatus(0); // 恢复待领养
        updateById(pet);
    }

    @Override
    public IPage<PetResponse> getPetList(PetQueryRequest request, Long shelterId) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Pet> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Pet> wrapper = Wrappers.lambdaQuery(Pet.class)
                .eq(Pet::getShelterId, shelterId)
                .eq(request.getStatus() != null, Pet::getStatus, request.getStatus())
                .eq(request.getType() != null, Pet::getType, request.getType())
                .eq(request.getBreed() != null, Pet::getBreed, request.getBreed());
        IPage<Pet> result = page(page, wrapper);
        IPage<PetResponse> responsePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toPetResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    public IPage<PetResponse> searchPets(PetSearchRequest request) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Pet> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Pet> wrapper = Wrappers.lambdaQuery(Pet.class)
                .eq(Pet::getStatus, 0) // 只搜索待领养
                .and(request.getKeyword() != null, w -> w
                    .like(Pet::getName, request.getKeyword())
                    .or().like(Pet::getType, request.getKeyword())
                    .or().like(Pet::getBreed, request.getKeyword())
                    .or().like(Pet::getColor, request.getKeyword())
                    .or().like(Pet::getDescription, request.getKeyword()))
                .eq(request.getType() != null, Pet::getType, request.getType())
                .eq(request.getSpecies() != null, Pet::getType, request.getSpecies())
                .eq(request.getBreed() != null, Pet::getBreed, request.getBreed())
                .ge(request.getMinAge() != null, Pet::getAge, request.getMinAge())
                .le(request.getMaxAge() != null, Pet::getAge, request.getMaxAge())
                .eq(request.getGender() != null, Pet::getGender, request.getGender())
                .eq(request.getVaccinated() != null, Pet::getVaccinated, request.getVaccinated())
                .eq(request.getSterilized() != null, Pet::getSterilized, request.getSterilized())
                .and(request.getSizeLevel() != null && !request.getSizeLevel().isEmpty(), w -> {
                    if ("小型".equals(request.getSizeLevel())) {
                        w.isNull(Pet::getSizeLevel).or().eq(Pet::getSizeLevel, "小型");
                    } else {
                        w.eq(Pet::getSizeLevel, request.getSizeLevel());
                    }
                })
                .eq(request.getActivityLevel() != null && !request.getActivityLevel().isEmpty(), Pet::getActivityLevel, request.getActivityLevel());
        if ("hot".equals(request.getSortBy())) {
            wrapper.orderByDesc(Pet::getCreateTime);
        } else {
            wrapper.orderByDesc(Pet::getCreateTime);
        }
        IPage<Pet> result = page(page, wrapper);
        IPage<PetResponse> responsePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toPetResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    public PetDetailResponse getPetDetail(Long petId) {
        Pet pet = getById(petId);
        if (pet == null) {
            throw new com.pawmatch.exception.BusinessException(404, "宠物不存在");
        }
        PetDetailResponse response = new PetDetailResponse();
        BeanUtils.copyProperties(pet, response);
        Shelter shelter = shelterMapper.selectById(pet.getShelterId());
        if (shelter != null) {
            ShelterResponse sr = new ShelterResponse();
            BeanUtils.copyProperties(shelter, sr);
            response.setShelter(sr);
        }
        return response;
    }

    private PetResponse toPetResponse(Pet pet) {
        PetResponse r = new PetResponse();
        BeanUtils.copyProperties(pet, r);
        if (r.getSizeLevel() == null || r.getSizeLevel().isEmpty()) {
            r.setSizeLevel("小型");
        }
        if (r.getActivityLevel() == null || r.getActivityLevel().isEmpty()) {
            r.setActivityLevel("温顺安静");
        }
        Shelter shelter = shelterMapper.selectById(pet.getShelterId());
        if (shelter != null) {
            r.setShelterName(shelter.getNickname());
        }
        return r;
    }

    @Override
    public List<PetResponse> recommendForUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getPetPreference() == null || user.getPetPreference().isBlank()) {
            return List.of();
        }
        String[] keywords = user.getPetPreference().split("[ ,，、;；\\s]+");
        LambdaQueryWrapper<Pet> wrapper = Wrappers.<Pet>lambdaQuery()
                .eq(Pet::getStatus, 0)
                .and(w -> {
                    for (String kw : keywords) {
                        if (kw.isBlank()) continue;
                        w.or(inner -> inner.like(Pet::getName, kw)
                                .or().like(Pet::getBreed, kw)
                                .or().like(Pet::getType, kw)
                                .or().like(Pet::getDescription, kw));
                    }
                })
                .orderByDesc(Pet::getCreateTime)
                .last("LIMIT 10");
        return list(wrapper).stream().map(this::toPetResponse).collect(Collectors.toList());
    }

    @Override
    public List<PetResponse> getCarouselPets() {
        LambdaQueryWrapper<Pet> wrapper = Wrappers.<Pet>lambdaQuery()
                .eq(Pet::getStatus, 0)
                .isNotNull(Pet::getImages)
                .ne(Pet::getImages, "")
                .orderByDesc(Pet::getCreateTime)
                .last("LIMIT 8");
        return list(wrapper).stream().map(pet -> {
            PetResponse r = toPetResponse(pet);
            // 只取第一张图片
            if (pet.getImages() != null && pet.getImages().contains(",")) {
                r.setImages(pet.getImages().split(",")[0]);
            }
            return r;
        }).collect(Collectors.toList());
    }
}
