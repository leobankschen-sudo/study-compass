package com.zhixu.studycompass.model;

import java.time.LocalDateTime;

public class UserPreference {
    private Long userId;
    private Integer weeklyTargetHours;
    private Integer preferredSessionMinutes;
    private String preferredStudyPeriod;
    private Boolean aiPersonalization;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getWeeklyTargetHours() { return weeklyTargetHours; }
    public void setWeeklyTargetHours(Integer weeklyTargetHours) { this.weeklyTargetHours = weeklyTargetHours; }
    public Integer getPreferredSessionMinutes() { return preferredSessionMinutes; }
    public void setPreferredSessionMinutes(Integer preferredSessionMinutes) { this.preferredSessionMinutes = preferredSessionMinutes; }
    public String getPreferredStudyPeriod() { return preferredStudyPeriod; }
    public void setPreferredStudyPeriod(String preferredStudyPeriod) { this.preferredStudyPeriod = preferredStudyPeriod; }
    public Boolean getAiPersonalization() { return aiPersonalization; }
    public void setAiPersonalization(Boolean aiPersonalization) { this.aiPersonalization = aiPersonalization; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
