package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class PetSearchRequest {

    private String keyword;
    private String type;
    private String species;
    private String breed;
    private String province;
    private String city;
    private Integer minAge;
    private Integer maxAge;
    private String gender;
    private Boolean vaccinated;
    private Boolean sterilized;
    private String sortBy;
    private String sortOrder;

    // 匹配画像筛选
    private String sizeLevel;
    private String activityLevel;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
