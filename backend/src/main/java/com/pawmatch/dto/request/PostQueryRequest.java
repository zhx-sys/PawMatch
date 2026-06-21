package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class PostQueryRequest {
    private String category;
    private String keyword;
    private String sortBy;
    private String sortOrder;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}