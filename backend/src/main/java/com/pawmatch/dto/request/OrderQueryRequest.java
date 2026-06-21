package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class OrderQueryRequest {

    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}