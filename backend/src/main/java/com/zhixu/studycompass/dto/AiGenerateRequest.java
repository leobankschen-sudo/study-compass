package com.zhixu.studycompass.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AiGenerateRequest(
        Long goalId,
        @Min(value = 3, message = "分析周期不能少于3天")
        @Max(value = 30, message = "分析周期不能超过30天")
        Integer days,
        @Size(max = 300, message = "补充说明不能超过300字")
        String note
) {
    public int effectiveDays() {
        return days == null ? 7 : days;
    }
}

