package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.dto.GoalRequest;
import com.zhixu.studycompass.model.LearningGoal;
import com.zhixu.studycompass.service.GoalService;
import com.zhixu.studycompass.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;
    private final CurrentUser currentUser;

    public GoalController(GoalService goalService, CurrentUser currentUser) {
        this.goalService = goalService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<List<LearningGoal>> findByUserId(@RequestParam Long userId) {
        currentUser.requireAccess(userId);
        return ApiResponse.ok(goalService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningGoal> findById(@PathVariable Long id) {
        LearningGoal goal = goalService.findById(id);
        currentUser.requireAccess(goal.getUserId());
        return ApiResponse.ok(goal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LearningGoal> create(@Valid @RequestBody GoalRequest request) {
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(goalService.create(request), "学习目标创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<LearningGoal> update(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        currentUser.requireAccess(goalService.findById(id).getUserId());
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(goalService.update(id, request), "学习目标更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        currentUser.requireAccess(goalService.findById(id).getUserId());
        goalService.delete(id);
        return ApiResponse.ok(null, "学习目标删除成功");
    }
}
