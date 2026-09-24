package com.zhixu.studycompass.service;

import com.zhixu.studycompass.domain.TaskStatus;
import com.zhixu.studycompass.dto.TaskRequest;
import com.zhixu.studycompass.model.LearningTask;

import java.util.List;

public interface TaskService {
    List<LearningTask> findByUserId(Long userId, TaskStatus status);
    LearningTask findById(Long id);
    LearningTask create(TaskRequest request);
    LearningTask update(Long id, TaskRequest request);
    void delete(Long id);
}

