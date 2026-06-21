package com.pawmatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateFosterOrderRequest {

    @NotNull(message = "寄养服务ID不能为空")
    private Long serviceId;

    @NotBlank(message = "宠物名称不能为空")
    private String petName;

    @NotBlank(message = "宠物类型不能为空")
    private String petType;

    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    @NotBlank(message = "结束日期不能为空")
    private String endDate;

    private String specialRequests;
}