package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.dto.StudyRecordRequest;
import com.zhixu.studycompass.model.StudyRecord;
import com.zhixu.studycompass.service.StudyRecordService;
import com.zhixu.studycompass.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class StudyRecordController {
    private final StudyRecordService recordService;
    private final CurrentUser currentUser;

    public StudyRecordController(StudyRecordService recordService, CurrentUser currentUser) {
        this.recordService = recordService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<List<StudyRecord>> findByUserId(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        currentUser.requireAccess(userId);
        return ApiResponse.ok(recordService.findByUserId(userId, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudyRecord> findById(@PathVariable Long id) {
        StudyRecord record = recordService.findById(id);
        currentUser.requireAccess(record.getUserId());
        return ApiResponse.ok(record);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudyRecord> create(@Valid @RequestBody StudyRecordRequest request) {
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(recordService.create(request), "学习记录创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<StudyRecord> update(@PathVariable Long id, @Valid @RequestBody StudyRecordRequest request) {
        currentUser.requireAccess(recordService.findById(id).getUserId());
        currentUser.requireAccess(request.userId());
        return ApiResponse.ok(recordService.update(id, request), "学习记录更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        currentUser.requireAccess(recordService.findById(id).getUserId());
        recordService.delete(id);
        return ApiResponse.ok(null, "学习记录删除成功");
    }
}
