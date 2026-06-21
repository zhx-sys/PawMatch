package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long petId;
    private String petName;
    private String petType;
    private Long userId;
    private String userName;
    private Long shelterId;
    private String reason;
    private String experience;
    private String housingCondition;
    private Integer status;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime completeTime;
}