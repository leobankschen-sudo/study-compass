package com.zhixu.studycompass.dto;

import com.zhixu.studycompass.domain.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LearningResourceRequest(
        @NotNull(message = "所属用户不能为空") Long userId,
        Long goalId,
        @NotBlank(message = "资料标题不能为空")
        @Size(max = 120, message = "资料标题不能超过120字") String title,
        @NotNull(message = "资料类型不能为空") ResourceType type,
        @Size(max = 500, message = "来源地址不能超过500字") String sourceUrl,
        @Size(max = 2000, message = "资料备注不能超过2000字") String description,
        @Size(max = 200, message = "标签不能超过200字") String tags
) {}
