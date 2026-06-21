package com.pawmatch.dto.response;

import com.pawmatch.dto.response.ShelterResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FosterServiceDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String petType;
    private Double pricePerDay;
    private Integer maxCapacity;
    private List<String> availableDates;
    private String images;
    private ShelterResponse shelter;
    private Double rating;
    private Integer reviewCount;
    private Integer status;
    private LocalDateTime createTime;
}