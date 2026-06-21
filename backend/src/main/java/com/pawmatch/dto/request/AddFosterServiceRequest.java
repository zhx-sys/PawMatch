package com.pawmatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddFosterServiceRequest {

    @NotBlank(message = "服务标题不能为空")
    private String title;

    @NotBlank(message = "服务描述不能为空")
    private String description;

    @NotBlank(message = "宠物类型不能为空")
    private String petType;

    @NotNull(message = "每日价格不能为空")
    private Double pricePerDay;

    @NotNull(message = "最大容量不能为空")
    private Integer maxCapacity;

    private List<String> availableDates;
    private List<String> images;
}