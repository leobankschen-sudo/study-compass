package com.zhixu.studycompass.dto;

import java.util.List;

public record AiReportContent(
        String title,
        String summary,
        List<String> evidence,
        List<AiInsight> insights,
        List<AiAction> actions
) {
    public record AiInsight(String title, String detail) {}

    public record AiAction(String title, String rationale, Integer durationMinutes, String priority) {}
}

