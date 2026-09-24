package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.domain.GoalStatus;
import com.zhixu.studycompass.dto.GoalRequest;
import com.zhixu.studycompass.mapper.GoalMapper;
import com.zhixu.studycompass.mapper.TaskMapper;
import com.zhixu.studycompass.mapper.StudyRecordMapper;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.model.LearningGoal;
import com.zhixu.studycompass.service.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalMapper goalMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final StudyRecordMapper recordMapper;

    public GoalServiceImpl(GoalMapper goalMapper, TaskMapper taskMapper, UserMapper userMapper,
                           StudyRecordMapper recordMapper) {
        this.goalMapper = goalMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public List<LearningGoal> findByUserId(Long userId) {
        return goalMapper.findByUserId(userId);
    }

    @Override
    public LearningGoal findById(Long id) {
        return goalMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "学习目标不存在"));
    }

    @Override
    @Transactional
    public LearningGoal create(GoalRequest request) {
        validateRequest(request);
        LearningGoal goal = toGoal(new LearningGoal(), request);
        goalMapper.insert(goal);
        return findById(goal.getId());
    }

    @Override
    @Transactional
    public LearningGoal update(Long id, GoalRequest request) {
        LearningGoal goal = findById(id);
        validateRequest(request);
        goalMapper.update(toGoal(goal, request));
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        if (taskMapper.countByGoalId(id) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该目标下仍有学习任务，不能直接删除");
        }
        if (recordMapper.countByGoalId(id) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该目标下仍有学习记录，不能直接删除");
        }
        goalMapper.deleteById(id);
    }

    private void validateRequest(GoalRequest request) {
        if (request.targetDate().isBefore(request.startDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "截止日期不能早于开始日期");
        }
        if (userMapper.findById(request.userId()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所属用户不存在");
        }
    }

    private LearningGoal toGoal(LearningGoal goal, GoalRequest request) {
        goal.setUserId(request.userId());
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setStartDate(request.startDate());
        goal.setTargetDate(request.targetDate());
        goal.setWeeklyHours(request.weeklyHours());
        goal.setCurrentLevel(request.currentLevel());
        goal.setStatus(request.status() == null ? GoalStatus.ACTIVE : request.status());
        return goal;
    }
}
