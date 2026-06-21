package com.pawmatch.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePetRequest {

    private String name;
    private String type;
    private String breed;
    private String gender;
    private Integer age;
    private String color;
    private Double weight;
    private String healthStatus;
    private Boolean vaccinated;
    private Boolean sterilized;
    private String description;
    private List<String> images;
}
