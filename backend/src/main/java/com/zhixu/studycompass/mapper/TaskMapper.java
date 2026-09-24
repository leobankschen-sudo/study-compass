package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.domain.TaskStatus;
import com.zhixu.studycompass.model.LearningTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface TaskMapper {
    List<LearningTask> findByUserId(@Param("userId") Long userId, @Param("status") TaskStatus status);
    Optional<LearningTask> findById(Long id);
    int insert(LearningTask task);
    int update(LearningTask task);
    int deleteById(Long id);
    int countByGoalId(Long goalId);
    int countByUserId(Long userId);
}
