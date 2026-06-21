package com.pawmatch.dto.response;

import com.pawmatch.dto.response.ShelterResponse;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetDetailResponse {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private String gender;
    private Integer age;
    private String color;
    private Double weight;
    private String healthStatus;
    private Boolean vaccinated;
    private Boolean sterilized;
    private String description;
    private String images;
    private Integer status;

    // 匹配画像相关字段
    private String sizeLevel;
    private String activityLevel;
    private Boolean beginnerFriendly;
    private Boolean goodWithKids;
    private Boolean goodWithPets;

    private ShelterResponse shelter;
    private LocalDateTime createTime;
}