package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.model.UserPreference;

import java.util.Optional;

public interface UserPreferenceMapper {
    Optional<UserPreference> findByUserId(Long userId);
    int upsert(UserPreference preference);
}
