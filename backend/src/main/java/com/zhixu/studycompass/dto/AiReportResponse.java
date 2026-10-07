package com.zhixu.studycompass.dto;

import com.zhixu.studycompass.domain.AiReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AiReportResponse(
        Long id,
        AiReportType type,
        Long goalId,
        LocalDate periodStart,
        LocalDate periodEnd,
        String model,
        LocalDateTime createdAt,
        SourceStats sourceStats,
        AiReportContent content
) {
    public record SourceStats(
            int recordCount,
            int totalMinutes,
            int completedTasks,
            Integer averageFocus
    ) {}
}

