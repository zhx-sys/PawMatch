package com.pawmatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdoptionApplicationRequest {

    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    @NotBlank(message = "申请理由不能为空")
    private String reason;

    @NotBlank(message = "养宠经验不能为空")
    private String experience;

    @NotBlank(message = "住房条件不能为空")
    private String housingCondition;

    private Boolean confirmFlood;
}