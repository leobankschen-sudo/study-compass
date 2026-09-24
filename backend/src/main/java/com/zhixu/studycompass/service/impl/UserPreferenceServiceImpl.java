package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.dto.UserPreferenceRequest;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.mapper.UserPreferenceMapper;
import com.zhixu.studycompass.model.UserPreference;
import com.zhixu.studycompass.service.UserPreferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {
    private final UserPreferenceMapper preferenceMapper;
    private final UserMapper userMapper;

    public UserPreferenceServiceImpl(UserPreferenceMapper preferenceMapper, UserMapper userMapper) {
        this.preferenceMapper = preferenceMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserPreference findByUserId(Long userId) {
        ensureUserExists(userId);
        return preferenceMapper.findByUserId(userId).orElseGet(() -> {
            UserPreference preference = defaults(userId);
            preferenceMapper.upsert(preference);
            return preferenceMapper.findByUserId(userId).orElseThrow();
        });
    }

    @Override
    @Transactional
    public UserPreference update(Long userId, UserPreferenceRequest request) {
        ensureUserExists(userId);
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setWeeklyTargetHours(request.weeklyTargetHours());
        preference.setPreferredSessionMinutes(request.preferredSessionMinutes());
        preference.setPreferredStudyPeriod(request.preferredStudyPeriod());
        preference.setAiPersonalization(request.aiPersonalization() == null || request.aiPersonalization());
        preferenceMapper.upsert(preference);
        return preferenceMapper.findByUserId(userId).orElseThrow();
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.findById(userId).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
    }

    private UserPreference defaults(Long userId) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setWeeklyTargetHours(10);
        preference.setPreferredSessionMinutes(45);
        preference.setPreferredStudyPeriod("EVENING");
        preference.setAiPersonalization(true);
        return preference;
    }
}
