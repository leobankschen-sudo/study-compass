package com.zhixu.studycompass.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserPreferenceRequest(
        @NotNull(message = "每周学习目标不能为空") @Min(value = 1, message = "每周学习目标至少为1小时")
        @Max(value = 80, message = "每周学习目标不能超过80小时") Integer weeklyTargetHours,
        @NotNull(message = "单次学习时长不能为空") @Min(value = 10, message = "单次学习时长至少为10分钟")
        @Max(value = 240, message = "单次学习时长不能超过240分钟") Integer preferredSessionMinutes,
        @NotBlank(message = "偏好学习时段不能为空")
        @Pattern(regexp = "MORNING|AFTERNOON|EVENING|FLEXIBLE", message = "偏好学习时段不正确") String preferredStudyPeriod,
        Boolean aiPersonalization
) {}
