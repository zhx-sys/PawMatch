package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.Shelter;
import com.pawmatch.dto.request.UpdateShelterRequest;
import com.pawmatch.dto.response.ShelterResponse;

public interface ShelterService extends IService<Shelter> {
    void updateShelterInfo(UpdateShelterRequest request, Long shelterId);
    ShelterResponse getShelterInfo(Long shelterId);
    boolean checkInfoComplete(Long shelterId);
}
