package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.dto.AuthResponse;
import com.zhixu.studycompass.dto.LoginRequest;
import com.zhixu.studycompass.dto.RegisterRequest;
import com.zhixu.studycompass.model.User;
import com.zhixu.studycompass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "登录成功");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request), "注册成功");
    }

    @GetMapping("/me")
    public ApiResponse<User> me() { return ApiResponse.ok(authService.currentUser()); }
}
