package com.zhixu.studycompass.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudyRecord {
    private Long id;
    private Long userId;
    private Long goalId;
    private Long taskId;
    private LocalDate studyDate;
    private Integer durationMinutes;
    private String content;
    private String reflection;
    private Integer focusScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public LocalDate getStudyDate() { return studyDate; }
    public void setStudyDate(LocalDate studyDate) { this.studyDate = studyDate; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReflection() { return reflection; }
    public void setReflection(String reflection) { this.reflection = reflection; }
    public Integer getFocusScore() { return focusScore; }
    public void setFocusScore(Integer focusScore) { this.focusScore = focusScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
