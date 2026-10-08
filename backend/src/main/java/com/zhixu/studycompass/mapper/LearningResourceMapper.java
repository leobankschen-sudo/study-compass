package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.domain.ResourceType;
import com.zhixu.studycompass.model.LearningResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface LearningResourceMapper {
    List<LearningResource> findByUserId(@Param("userId") Long userId,
                                        @Param("type") ResourceType type,
                                        @Param("goalId") Long goalId);
    Optional<LearningResource> findById(Long id);
    int insert(LearningResource resource);
    int update(LearningResource resource);
    int deleteById(Long id);
}
