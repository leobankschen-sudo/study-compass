package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.domain.AiReportType;
import com.zhixu.studycompass.dto.AiGenerateRequest;
import com.zhixu.studycompass.dto.AiReportResponse;
import com.zhixu.studycompass.service.AiCoachService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/reports")
public class AiCoachController {
    private final AiCoachService aiCoachService;

    public AiCoachController(AiCoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    @GetMapping
    public ApiResponse<List<AiReportResponse>> list(@RequestParam(required = false) AiReportType type) {
        return ApiResponse.ok(aiCoachService.list(type));
    }

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiReportResponse> generate(@PathVariable AiReportType type,
                                                   @Valid @RequestBody AiGenerateRequest request) {
        return ApiResponse.ok(aiCoachService.generate(type, request),
                type == AiReportType.REVIEW ? "智能复盘生成成功" : "学习计划生成成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        aiCoachService.delete(id);
        return ApiResponse.ok(null, "历史报告已删除");
    }
}

