package com.pawmatch.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private Integer userType;
}