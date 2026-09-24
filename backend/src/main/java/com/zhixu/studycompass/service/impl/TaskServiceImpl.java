package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.domain.TaskPriority;
import com.zhixu.studycompass.domain.TaskStatus;
import com.zhixu.studycompass.dto.TaskRequest;
import com.zhixu.studycompass.mapper.GoalMapper;
import com.zhixu.studycompass.mapper.TaskMapper;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.mapper.StudyRecordMapper;
import com.zhixu.studycompass.model.LearningTask;
import com.zhixu.studycompass.model.LearningGoal;
import com.zhixu.studycompass.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskMapper taskMapper;
    private final GoalMapper goalMapper;
    private final UserMapper userMapper;
    private final StudyRecordMapper recordMapper;

    public TaskServiceImpl(TaskMapper taskMapper, GoalMapper goalMapper, UserMapper userMapper,
                           StudyRecordMapper recordMapper) {
        this.taskMapper = taskMapper;
        this.goalMapper = goalMapper;
        this.userMapper = userMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public List<LearningTask> findByUserId(Long userId, TaskStatus status) {
        return taskMapper.findByUserId(userId, status);
    }

    @Override
    public LearningTask findById(Long id) {
        return taskMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "学习任务不存在"));
    }

    @Override
    @Transactional
    public LearningTask create(TaskRequest request) {
        validateRelations(request);
        LearningTask task = toTask(new LearningTask(), request);
        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        taskMapper.insert(task);
        return findById(task.getId());
    }

    @Override
    @Transactional
    public LearningTask update(Long id, TaskRequest request) {
        LearningTask task = findById(id);
        validateRelations(request);
        TaskStatus previousStatus = task.getStatus();
        task = toTask(task, request);
        if (task.getStatus() == TaskStatus.COMPLETED && previousStatus != TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (task.getStatus() != TaskStatus.COMPLETED) {
            task.setCompletedAt(null);
        }
        taskMapper.update(task);
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        if (recordMapper.countByTaskId(id) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该任务已有学习记录，不能直接删除");
        }
        taskMapper.deleteById(id);
    }

    private void validateRelations(TaskRequest request) {
        if (userMapper.findById(request.userId()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所属用户不存在");
        }
        LearningGoal goal = goalMapper.findById(request.goalId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "所属学习目标不存在"));
        if (!goal.getUserId().equals(request.userId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "学习任务与所属目标的用户不一致");
        }
    }

    private LearningTask toTask(LearningTask task, TaskRequest request) {
        task.setGoalId(request.goalId());
        task.setUserId(request.userId());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setStatus(request.status() == null ? TaskStatus.TODO : request.status());
        task.setDueDate(request.dueDate());
        task.setEstimatedMinutes(request.estimatedMinutes());
        task.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        task.setAiGenerated(request.aiGenerated() != null && request.aiGenerated());
        return task;
    }
}
