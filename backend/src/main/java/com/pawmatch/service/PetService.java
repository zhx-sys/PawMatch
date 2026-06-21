package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.Pet;
import com.pawmatch.dto.request.AddPetRequest;
import com.pawmatch.dto.request.UpdatePetRequest;
import com.pawmatch.dto.request.PetQueryRequest;
import com.pawmatch.dto.request.PetSearchRequest;
import com.pawmatch.dto.response.PetResponse;
import com.pawmatch.dto.response.PetDetailResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public interface PetService extends IService<Pet> {
    Long addPet(AddPetRequest request, Long shelterId);
    void updatePet(UpdatePetRequest request, Long petId);
    void deletePet(Long petId, Long shelterId);
    void restorePet(Long petId, Long shelterId);
    IPage<PetResponse> getPetList(PetQueryRequest request, Long shelterId);
    IPage<PetResponse> searchPets(PetSearchRequest request);
    PetDetailResponse getPetDetail(Long petId);
    List<PetResponse> recommendForUser(Long userId);
    List<PetResponse> getCarouselPets();
}
