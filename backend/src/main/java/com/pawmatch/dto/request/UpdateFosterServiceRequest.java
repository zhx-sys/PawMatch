package com.pawmatch.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateFosterServiceRequest {

    private String title;
    private String description;
    private String petType;
    private Double pricePerDay;
    private Integer maxCapacity;
    private List<String> availableDates;
    private List<String> images;
}