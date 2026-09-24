package com.zhixu.studycompass.dto;

import com.zhixu.studycompass.domain.GoalStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GoalRequest(
        @NotNull(message = "用户不能为空") Long userId,
        @NotBlank(message = "目标名称不能为空") @Size(max = 80, message = "目标名称不能超过80个字") String title,
        @Size(max = 1000, message = "目标说明不能超过1000个字") String description,
        @NotNull(message = "开始日期不能为空") LocalDate startDate,
        @NotNull(message = "截止日期不能为空") @FutureOrPresent(message = "截止日期不能早于今天") LocalDate targetDate,
        @Min(value = 1, message = "每周学习时间至少为1小时") @Max(value = 80, message = "每周学习时间不能超过80小时") Integer weeklyHours,
        @Size(max = 200, message = "当前基础不能超过200个字") String currentLevel,
        GoalStatus status
) {}

