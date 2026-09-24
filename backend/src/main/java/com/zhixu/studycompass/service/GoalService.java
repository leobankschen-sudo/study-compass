package com.zhixu.studycompass.service;

import com.zhixu.studycompass.dto.GoalRequest;
import com.zhixu.studycompass.model.LearningGoal;

import java.util.List;

public interface GoalService {
    List<LearningGoal> findByUserId(Long userId);
    LearningGoal findById(Long id);
    LearningGoal create(GoalRequest request);
    LearningGoal update(Long id, GoalRequest request);
    void delete(Long id);
}

