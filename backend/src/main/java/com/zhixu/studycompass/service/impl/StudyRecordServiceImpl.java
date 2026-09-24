package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.dto.StudyRecordRequest;
import com.zhixu.studycompass.mapper.GoalMapper;
import com.zhixu.studycompass.mapper.StudyRecordMapper;
import com.zhixu.studycompass.mapper.TaskMapper;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.model.LearningGoal;
import com.zhixu.studycompass.model.LearningTask;
import com.zhixu.studycompass.model.StudyRecord;
import com.zhixu.studycompass.service.StudyRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudyRecordServiceImpl implements StudyRecordService {
    private final StudyRecordMapper recordMapper;
    private final UserMapper userMapper;
    private final GoalMapper goalMapper;
    private final TaskMapper taskMapper;

    public StudyRecordServiceImpl(StudyRecordMapper recordMapper, UserMapper userMapper,
                                  GoalMapper goalMapper, TaskMapper taskMapper) {
        this.recordMapper = recordMapper;
        this.userMapper = userMapper;
        this.goalMapper = goalMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<StudyRecord> findByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "结束日期不能早于开始日期");
        }
        return recordMapper.findByUserId(userId, startDate, endDate);
    }

    @Override
    public StudyRecord findById(Long id) {
        return recordMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "学习记录不存在"));
    }

    @Override
    @Transactional
    public StudyRecord create(StudyRecordRequest request) {
        validateRelations(request);
        StudyRecord record = toRecord(new StudyRecord(), request);
        recordMapper.insert(record);
        return findById(record.getId());
    }

    @Override
    @Transactional
    public StudyRecord update(Long id, StudyRecordRequest request) {
        StudyRecord record = findById(id);
        validateRelations(request);
        recordMapper.update(toRecord(record, request));
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        recordMapper.deleteById(id);
    }

    private void validateRelations(StudyRecordRequest request) {
        if (userMapper.findById(request.userId()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所属用户不存在");
        }
        LearningGoal goal = goalMapper.findById(request.goalId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "所属学习目标不存在"));
        if (!goal.getUserId().equals(request.userId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "学习记录与所属目标的用户不一致");
        }
        if (request.taskId() != null) {
            LearningTask task = taskMapper.findById(request.taskId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "关联任务不存在"));
            if (!task.getUserId().equals(request.userId()) || !task.getGoalId().equals(request.goalId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "关联任务不属于当前学习目标");
            }
        }
    }

    private StudyRecord toRecord(StudyRecord record, StudyRecordRequest request) {
        record.setUserId(request.userId());
        record.setGoalId(request.goalId());
        record.setTaskId(request.taskId());
        record.setStudyDate(request.studyDate());
        record.setDurationMinutes(request.durationMinutes());
        record.setContent(request.content());
        record.setReflection(request.reflection());
        record.setFocusScore(request.focusScore());
        return record;
    }
}
