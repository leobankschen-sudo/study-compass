package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.model.LearningGoal;

import java.util.List;
import java.util.Optional;

public interface GoalMapper {
    List<LearningGoal> findByUserId(Long userId);
    Optional<LearningGoal> findById(Long id);
    int insert(LearningGoal goal);
    int update(LearningGoal goal);
    int deleteById(Long id);
    int countByUserId(Long userId);
}
