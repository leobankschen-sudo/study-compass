package com.zhixu.studycompass.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudyRecordRequest(
        @NotNull(message = "用户不能为空") Long userId,
        @NotNull(message = "学习目标不能为空") Long goalId,
        Long taskId,
        @NotNull(message = "学习日期不能为空") @PastOrPresent(message = "学习日期不能晚于今天") LocalDate studyDate,
        @NotNull(message = "学习时长不能为空") @Min(value = 5, message = "学习时长不能少于5分钟")
        @Max(value = 1440, message = "单次学习时长不能超过1440分钟") Integer durationMinutes,
        @NotBlank(message = "学习内容不能为空") @Size(max = 500, message = "学习内容不能超过500个字") String content,
        @Size(max = 1000, message = "学习反思不能超过1000个字") String reflection,
        @Min(value = 1, message = "专注度最低为1") @Max(value = 5, message = "专注度最高为5") Integer focusScore
) {}
