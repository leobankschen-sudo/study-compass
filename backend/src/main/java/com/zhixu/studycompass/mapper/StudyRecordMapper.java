package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.model.StudyRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyRecordMapper {
    List<StudyRecord> findByUserId(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    Optional<StudyRecord> findById(Long id);
    int insert(StudyRecord record);
    int update(StudyRecord record);
    int deleteById(Long id);
    int countByGoalId(Long goalId);
    int countByTaskId(Long taskId);
    int countByUserId(Long userId);
}
