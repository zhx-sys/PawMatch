package com.pawmatch.security;

import java.security.Principal;

public class PawMatchPrincipal implements Principal {

    private final Long userId;
    private final Integer userType;

    public PawMatchPrincipal(Long userId, Integer userType) {
        this.userId = userId;
        this.userType = userType;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getUserType() {
        return userType;
    }
}
