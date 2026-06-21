package com.pawmatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddPetRequest {

    @NotBlank(message = "宠物名称不能为空")
    private String name;

    @NotBlank(message = "宠物类型不能为空")
    private String type;

    @NotBlank(message = "品种不能为空")
    private String breed;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    private String color;
    private Double weight;

    @NotBlank(message = "健康状况不能为空")
    private String healthStatus;

    @NotNull(message = "疫苗接种情况不能为空")
    private Boolean vaccinated;

    @NotNull(message = "绝育情况不能为空")
    private Boolean sterilized;

    private String description;

    // 匹配画像相关字段
    private String sizeLevel;        // 体型: 小型/中型/大型
    private String activityLevel;    // 活跃度: 安静/中等/活跃
    private Boolean beginnerFriendly; // 适合新手
    private Boolean goodWithKids;    // 适合儿童
    private Boolean goodWithPets;    // 合群(适合多宠家庭)

    private List<String> images;
}
