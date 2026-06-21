package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class PetQueryRequest {

    private Integer status;
    private String type;
    private String breed;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
