package com.zhixu.studycompass.dto;

import com.zhixu.studycompass.model.User;

import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt, User user) {}
