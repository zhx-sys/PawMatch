package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class ApplicationQueryRequest {

    private Integer status;
    private Long petId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}