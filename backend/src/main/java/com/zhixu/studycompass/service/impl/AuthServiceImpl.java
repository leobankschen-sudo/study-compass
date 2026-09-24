package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.dto.AuthResponse;
import com.zhixu.studycompass.dto.LoginRequest;
import com.zhixu.studycompass.dto.RegisterRequest;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.model.User;
import com.zhixu.studycompass.security.CurrentUser;
import com.zhixu.studycompass.security.JwtService;
import com.zhixu.studycompass.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUser currentUser;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService,
                           CurrentUser currentUser) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.currentUser = currentUser;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userMapper.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        return response(user);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.existsByUsernameOrEmail(request.username(), request.email(), null)) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名或邮箱已被使用");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setEnabled(true);
        userMapper.insertWithPassword(user);
        return response(userMapper.findById(user.getId()).orElseThrow());
    }

    @Override
    public User currentUser() { return currentUser.require(); }

    private AuthResponse response(User user) {
        JwtService.TokenResult token = jwtService.issue(user);
        return new AuthResponse(token.token(), token.expiresAt(), user);
    }
}
