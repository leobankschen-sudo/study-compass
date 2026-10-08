package com.zhixu.studycompass.service;

import com.zhixu.studycompass.domain.ResourceType;
import com.zhixu.studycompass.dto.LearningResourceRequest;
import com.zhixu.studycompass.model.LearningResource;

import java.util.List;

public interface LearningResourceService {
    List<LearningResource> findByUserId(Long userId, ResourceType type, Long goalId);
    LearningResource findById(Long id);
    LearningResource create(LearningResourceRequest request);
    LearningResource update(Long id, LearningResourceRequest request);
    void delete(Long id);
}
