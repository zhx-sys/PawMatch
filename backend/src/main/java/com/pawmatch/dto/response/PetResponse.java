package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetResponse {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private String gender;
    private Integer age;
    private String color;
    private String healthStatus;
    private Boolean vaccinated;
    private Boolean sterilized;
    private String images;
    private Integer status;
    private Long shelterId;
    private String shelterName;

    // 匹配画像相关字段
    private String sizeLevel;
    private String activityLevel;
    private Boolean beginnerFriendly;
    private Boolean goodWithKids;
    private Boolean goodWithPets;

    private LocalDateTime createTime;
}