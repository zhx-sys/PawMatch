package com.pawmatch.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawmatch.entity.Shelter;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.util.PasswordUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ShelterDataInit {

    private final ShelterMapper shelterMapper;

    public ShelterDataInit(ShelterMapper shelterMapper) {
        this.shelterMapper = shelterMapper;
    }

    @PostConstruct
    public void init() {
        List<Shelter> shelters = shelterMapper.selectList(
                new LambdaQueryWrapper<Shelter>().orderByAsc(Shelter::getId));
        if (shelters.isEmpty()) {
            Shelter shelter = new Shelter();
            shelter.setAccount("100001");
            shelter.setPassword(PasswordUtil.encode("12345678z"));
            shelter.setNickname("");
            shelter.setManagerName("");
            shelter.setPhone("");
            shelter.setProvince("");
            shelter.setCity("");
            shelter.setAddressDetail("");
            shelter.setCreditScore(100);
            shelter.setStatus(1);
            shelter.setInfoComplete(false);
            shelter.setCreateTime(LocalDateTime.now());
            shelter.setUpdateTime(LocalDateTime.now());
            shelterMapper.insert(shelter);
        } else {
            // remove extra shelters to avoid unique constraint violation
            for (int i = 1; i < shelters.size(); i++) {
                shelterMapper.deleteById(shelters.get(i).getId());
            }
            // keep existing shelter data, do NOT reset password
        }
    }
}