package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FosterOrderResponse {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long shelterId;
    private String shelterName;
    private String petName;
    private String petType;
    private String startDate;
    private String endDate;
    private Integer totalDays;
    private Double totalPrice;
    private String specialRequests;
    private Integer status;
    private Integer rating;
    private String comment;
    private LocalDateTime createTime;
}