package com.pawmatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.Shelter;
import com.pawmatch.dto.request.RegisterUserRequest;
import com.pawmatch.dto.request.LoginRequest;
import com.pawmatch.dto.request.ForgotPasswordRequest;
import com.pawmatch.dto.response.LoginResponse;
import com.pawmatch.dto.response.RegisterResponse;

public interface AuthService extends IService<Shelter> {
    RegisterResponse registerUser(RegisterUserRequest request);
    LoginResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
}
