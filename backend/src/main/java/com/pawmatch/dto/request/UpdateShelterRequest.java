package com.pawmatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShelterRequest {

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "负责人姓名不能为空")
    private String managerName;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "详细地址不能为空")
    private String addressDetail;

    private String introduction;
}
