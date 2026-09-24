package com.zhixu.studycompass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9_]{4,30}$", message = "用户名须为4至30位字母、数字或下划线") String username,
        @NotBlank(message = "昵称不能为空") @Size(max = 40, message = "昵称不能超过40个字") String nickname,
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$", message = "密码至少8位，且必须包含字母和数字") String password
) {}
