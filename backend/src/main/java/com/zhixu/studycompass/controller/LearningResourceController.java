package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.domain.ResourceType;
import com.zhixu.studycompass.dto.LearningResourceRequest;
import com.zhixu.studycompass.model.LearningResource;
import com.zhixu.studycompass.security.CurrentUser;
import com.zhixu.studycompass.service.LearningResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class LearningResourceController {
    private final LearningResourceService resourceService;
    private final CurrentUser currentUser;

    public LearningResourceController(LearningResourceService resourceService, CurrentUser currentUser) {
        this.resourceService = resourceService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<List<LearningResource>> list(@RequestParam Long userId,
                                                     @RequestParam(required = false) ResourceType type,
                                                     @RequestParam(required = false) Long goalId) {
        currentUser.requireAccess(userId);
        return ApiResponse.ok(resourceService.findByUserId(userId, type, goalId));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningResource> findById(@PathVariable Long id) {
        LearningResource resource = resourceService.findById(id);
        currentUser.requireAccess(resource.getUserId());
        return ApiResponse.ok(resource);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LearningResource> create(@Valid @RequestBody LearningResourceRequest request) {
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(resourceService.create(request), "学习资料创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<LearningResource> update(@PathVariable Long id,
                                                 @Valid @RequestBody LearningResourceRequest request) {
        currentUser.requireAccess(resourceService.findById(id).getUserId());
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(resourceService.update(id, request), "学习资料更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        currentUser.requireAccess(resourceService.findById(id).getUserId());
        resourceService.delete(id);
        return ApiResponse.ok(null, "学习资料删除成功");
    }
}
