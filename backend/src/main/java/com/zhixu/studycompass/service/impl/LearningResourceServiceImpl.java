package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.domain.ResourceType;
import com.zhixu.studycompass.dto.LearningResourceRequest;
import com.zhixu.studycompass.mapper.GoalMapper;
import com.zhixu.studycompass.mapper.LearningResourceMapper;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.model.LearningGoal;
import com.zhixu.studycompass.model.LearningResource;
import com.zhixu.studycompass.service.LearningResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LearningResourceServiceImpl implements LearningResourceService {
    private final LearningResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final GoalMapper goalMapper;

    public LearningResourceServiceImpl(LearningResourceMapper resourceMapper,
                                       UserMapper userMapper,
                                       GoalMapper goalMapper) {
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
        this.goalMapper = goalMapper;
    }

    @Override
    public List<LearningResource> findByUserId(Long userId, ResourceType type, Long goalId) {
        if (userMapper.findById(userId).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (goalId != null) requireGoalAccess(goalId, userId);
        return resourceMapper.findByUserId(userId, type, goalId);
    }

    @Override
    public LearningResource findById(Long id) {
        return resourceMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "学习资料不存在"));
    }

    @Override
    @Transactional
    public LearningResource create(LearningResourceRequest request) {
        validateRelations(request);
        LearningResource resource = apply(new LearningResource(), request);
        resourceMapper.insert(resource);
        return findById(resource.getId());
    }

    @Override
    @Transactional
    public LearningResource update(Long id, LearningResourceRequest request) {
        LearningResource resource = findById(id);
        if (!resource.getUserId().equals(request.userId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能修改资料的所属用户");
        }
        validateRelations(request);
        resourceMapper.update(apply(resource, request));
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        resourceMapper.deleteById(id);
    }

    private void validateRelations(LearningResourceRequest request) {
        if (userMapper.findById(request.userId()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所属用户不存在");
        }
        if (request.goalId() != null) requireGoalAccess(request.goalId(), request.userId());
    }

    private void requireGoalAccess(Long goalId, Long userId) {
        LearningGoal goal = goalMapper.findById(goalId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "关联学习目标不存在"));
        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "资料与关联目标的用户不一致");
        }
    }

    private LearningResource apply(LearningResource resource, LearningResourceRequest request) {
        resource.setUserId(request.userId());
        resource.setGoalId(request.goalId());
        resource.setTitle(request.title().trim());
        resource.setType(request.type());
        resource.setSourceUrl(blankToNull(request.sourceUrl()));
        resource.setDescription(blankToNull(request.description()));
        resource.setTags(blankToNull(request.tags()));
        return resource;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
