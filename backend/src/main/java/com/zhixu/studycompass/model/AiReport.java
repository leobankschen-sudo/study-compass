package com.zhixu.studycompass.model;

import com.zhixu.studycompass.domain.AiReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AiReport {
    private Long id;
    private Long userId;
    private Long goalId;
    private AiReportType type;
    private String title;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String contentJson;
    private String model;
    private Integer recordCount;
    private Integer totalMinutes;
    private Integer completedTasks;
    private Integer averageFocus;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public AiReportType getType() { return type; }
    public void setType(AiReportType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }
    public Integer getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Integer totalMinutes) { this.totalMinutes = totalMinutes; }
    public Integer getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Integer completedTasks) { this.completedTasks = completedTasks; }
    public Integer getAverageFocus() { return averageFocus; }
    public void setAverageFocus(Integer averageFocus) { this.averageFocus = averageFocus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

