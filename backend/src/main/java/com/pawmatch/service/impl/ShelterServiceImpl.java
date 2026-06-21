package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pawmatch.entity.Shelter;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.service.ShelterService;
import com.pawmatch.dto.request.UpdateShelterRequest;
import com.pawmatch.dto.response.ShelterResponse;
import org.springframework.stereotype.Service;

@Service
public class ShelterServiceImpl extends ServiceImpl<ShelterMapper, Shelter> implements ShelterService {

    @Override
    public void updateShelterInfo(UpdateShelterRequest request, Long shelterId) {
        Shelter shelter = this.getById(shelterId);
        if (shelter == null) {
            throw new com.pawmatch.exception.BusinessException(404, "救助站不存在");
        }
        shelter.setNickname(request.getNickname());
        shelter.setManagerName(request.getManagerName());
        shelter.setPhone(request.getPhone());
        shelter.setProvince(request.getProvince());
        shelter.setCity(request.getCity());
        shelter.setAddressDetail(request.getAddressDetail());
        shelter.setIntroduction(request.getIntroduction());
        shelter.setInfoComplete(true);
        this.updateById(shelter);
    }

    @Override
    public ShelterResponse getShelterInfo(Long shelterId) {
        Shelter shelter = this.getById(shelterId);
        if (shelter == null) {
            throw new com.pawmatch.exception.BusinessException(404, "救助站不存在");
        }
        ShelterResponse response = new ShelterResponse();
        response.setId(shelter.getId());
        response.setNickname(shelter.getNickname());
        response.setManagerName(shelter.getManagerName());
        response.setPhone(shelter.getPhone());
        response.setProvince(shelter.getProvince());
        response.setCity(shelter.getCity());
        response.setAddressDetail(shelter.getAddressDetail());
        response.setIntroduction(shelter.getIntroduction());
        response.setCreditScore(shelter.getCreditScore());
        response.setInfoComplete(shelter.getInfoComplete());
        response.setCreateTime(shelter.getCreateTime());
        return response;
    }

    @Override
    public boolean checkInfoComplete(Long shelterId) {
        Shelter shelter = this.getById(shelterId);
        return shelter != null && Boolean.TRUE.equals(shelter.getInfoComplete());
    }
}
