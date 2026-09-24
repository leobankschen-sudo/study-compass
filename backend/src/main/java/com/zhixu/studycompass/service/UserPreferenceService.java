package com.zhixu.studycompass.service;

import com.zhixu.studycompass.dto.UserPreferenceRequest;
import com.zhixu.studycompass.model.UserPreference;

public interface UserPreferenceService {
    UserPreference findByUserId(Long userId);
    UserPreference update(Long userId, UserPreferenceRequest request);
}
