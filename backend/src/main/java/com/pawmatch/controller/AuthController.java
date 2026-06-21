package com.pawmatch.controller;

import com.pawmatch.dto.request.RegisterUserRequest;
import com.pawmatch.dto.request.LoginRequest;
import com.pawmatch.dto.request.ForgotPasswordRequest;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.LoginResponse;
import com.pawmatch.dto.response.RegisterResponse;
import com.pawmatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/user")
    public ApiResponse<RegisterResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        RegisterResponse resp = authService.registerUser(request);
        return ApiResponse.success("注册成功", resp);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("密码重置成功", null);
    }
}
