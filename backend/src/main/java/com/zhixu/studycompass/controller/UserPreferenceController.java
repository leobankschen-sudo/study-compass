package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.dto.UserPreferenceRequest;
import com.zhixu.studycompass.model.UserPreference;
import com.zhixu.studycompass.service.UserPreferenceService;
import com.zhixu.studycompass.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferenceController {
    private final UserPreferenceService preferenceService;
    private final CurrentUser currentUser;

    public UserPreferenceController(UserPreferenceService preferenceService, CurrentUser currentUser) {
        this.preferenceService = preferenceService;
        this.currentUser = currentUser;
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserPreference> findByUserId(@PathVariable Long userId) {
        currentUser.requireAccess(userId);
        return ApiResponse.ok(preferenceService.findByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserPreference> update(@PathVariable Long userId,
                                               @Valid @RequestBody UserPreferenceRequest request) {
        currentUser.requireAccess(userId);
        return ApiResponse.ok(preferenceService.update(userId, request), "学习偏好更新成功");
    }
}
