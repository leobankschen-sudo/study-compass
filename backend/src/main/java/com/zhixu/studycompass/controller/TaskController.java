package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.domain.TaskStatus;
import com.zhixu.studycompass.dto.TaskRequest;
import com.zhixu.studycompass.model.LearningTask;
import com.zhixu.studycompass.service.TaskService;
import com.zhixu.studycompass.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final CurrentUser currentUser;

    public TaskController(TaskService taskService, CurrentUser currentUser) {
        this.taskService = taskService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<List<LearningTask>> findByUserId(
            @RequestParam Long userId,
            @RequestParam(required = false) TaskStatus status) {
        currentUser.requireAccess(userId);
        return ApiResponse.ok(taskService.findByUserId(userId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningTask> findById(@PathVariable Long id) {
        LearningTask task = taskService.findById(id);
        currentUser.requireAccess(task.getUserId());
        return ApiResponse.ok(task);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LearningTask> create(@Valid @RequestBody TaskRequest request) {
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(taskService.create(request), "学习任务创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<LearningTask> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        currentUser.requireAccess(taskService.findById(id).getUserId());
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(taskService.update(id, request), "学习任务更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        currentUser.requireAccess(taskService.findById(id).getUserId());
        taskService.delete(id);
        return ApiResponse.ok(null, "学习任务删除成功");
    }
}
