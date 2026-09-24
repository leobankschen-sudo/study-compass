package com.zhixu.studycompass.service;

import com.zhixu.studycompass.dto.AuthResponse;
import com.zhixu.studycompass.dto.LoginRequest;
import com.zhixu.studycompass.dto.RegisterRequest;
import com.zhixu.studycompass.model.User;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    User currentUser();
}
