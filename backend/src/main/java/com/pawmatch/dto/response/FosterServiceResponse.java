package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FosterServiceResponse {
    private Long id;
    private String title;
    private String description;
    private String petType;
    private Double pricePerDay;
    private Integer maxCapacity;
    private String images;
    private Long shelterId;
    private String shelterName;
    private Integer status;
    private LocalDateTime createTime;
}