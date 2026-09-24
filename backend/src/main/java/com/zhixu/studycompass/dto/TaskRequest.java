package com.zhixu.studycompass.dto;

import com.zhixu.studycompass.domain.TaskPriority;
import com.zhixu.studycompass.domain.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotNull(message = "学习目标不能为空") Long goalId,
        @NotNull(message = "用户不能为空") Long userId,
        @NotBlank(message = "任务名称不能为空") @Size(max = 120, message = "任务名称不能超过120个字") String title,
        @Size(max = 1000, message = "任务说明不能超过1000个字") String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDate dueDate,
        @Min(value = 5, message = "预计用时不能少于5分钟") @Max(value = 1440, message = "单项任务预计用时不能超过1440分钟") Integer estimatedMinutes,
        Integer sortOrder,
        Boolean aiGenerated
) {}

