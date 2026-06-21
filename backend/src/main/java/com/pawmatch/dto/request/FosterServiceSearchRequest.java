package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class FosterServiceSearchRequest {

    private String province;
    private String city;
    private String petType;
    private Double minPrice;
    private Double maxPrice;
    private String sortBy;
    private String sortOrder;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}