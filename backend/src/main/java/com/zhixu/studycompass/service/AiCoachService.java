package com.zhixu.studycompass.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.domain.AiReportType;
import com.zhixu.studycompass.domain.TaskStatus;
import com.zhixu.studycompass.dto.AiGenerateRequest;
import com.zhixu.studycompass.dto.AiReportContent;
import com.zhixu.studycompass.dto.AiReportResponse;
import com.zhixu.studycompass.mapper.AiReportMapper;
import com.zhixu.studycompass.mapper.GoalMapper;
import com.zhixu.studycompass.mapper.StudyRecordMapper;
import com.zhixu.studycompass.mapper.TaskMapper;
import com.zhixu.studycompass.mapper.UserPreferenceMapper;
import com.zhixu.studycompass.model.AiReport;
import com.zhixu.studycompass.model.LearningGoal;
import com.zhixu.studycompass.model.LearningTask;
import com.zhixu.studycompass.model.StudyRecord;
import com.zhixu.studycompass.model.User;
import com.zhixu.studycompass.model.UserPreference;
import com.zhixu.studycompass.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AiCoachService {
    private static final Logger log = LoggerFactory.getLogger(AiCoachService.class);
    private static final String SYSTEM_PROMPT = """
            你是学习管理系统“知序”的学习教练。仅根据用户提供的学习证据做判断，不编造成绩、课程或完成情况。
            输出必须是一个合法 JSON 对象，不要使用 Markdown，不要添加 JSON 之外的文字。
            JSON 结构严格如下：
            {
              "title": "不超过24字的标题",
              "summary": "80至160字的总结",
              "evidence": ["2至4条有数字或原始记录支持的证据"],
              "insights": [{"title": "判断标题", "detail": "判断依据和影响"}],
              "actions": [{"title": "可执行动作", "rationale": "为什么做", "durationMinutes": 45, "priority": "HIGH|MEDIUM|LOW"}]
            }
            insights 输出2至4项，actions 输出3至5项。行动时长必须在15至180分钟之间。
            如果数据不足，要明确指出不足，并给出补充记录的具体方式，不能假装已经掌握完整情况。
            """;

    private final CurrentUser currentUser;
    private final GoalMapper goalMapper;
    private final TaskMapper taskMapper;
    private final StudyRecordMapper recordMapper;
    private final UserPreferenceMapper preferenceMapper;
    private final AiReportMapper reportMapper;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AiCoachService(CurrentUser currentUser,
                          GoalMapper goalMapper,
                          TaskMapper taskMapper,
                          StudyRecordMapper recordMapper,
                          UserPreferenceMapper preferenceMapper,
                          AiReportMapper reportMapper,
                          ObjectMapper objectMapper,
                          @Value("${app.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                          @Value("${app.deepseek.api-key:}") String apiKey,
                          @Value("${app.deepseek.model:deepseek-flash}") String model) {
        this.currentUser = currentUser;
        this.goalMapper = goalMapper;
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.preferenceMapper = preferenceMapper;
        this.reportMapper = reportMapper;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(8));
        requestFactory.setReadTimeout(Duration.ofSeconds(50));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Transactional
    public AiReportResponse generate(AiReportType type, AiGenerateRequest request) {
        User user = currentUser.require();
        ensurePersonalizationEnabled(user.getId());
        LearningGoal selectedGoal = requireSelectedGoal(type, request.goalId(), user.getId());

        LocalDate periodEnd = LocalDate.now();
        LocalDate periodStart = periodEnd.minusDays(request.effectiveDays() - 1L);
        List<StudyRecord> records = recordMapper.findByUserId(user.getId(), periodStart, periodEnd).stream()
                .filter(record -> selectedGoal == null || selectedGoal.getId().equals(record.getGoalId()))
                .toList();
        List<LearningTask> tasks = taskMapper.findByUserId(user.getId(), null).stream()
                .filter(task -> selectedGoal == null || selectedGoal.getId().equals(task.getGoalId()))
                .toList();
        List<LearningGoal> goals = selectedGoal == null
                ? goalMapper.findByUserId(user.getId())
                : List.of(selectedGoal);

        if (type == AiReportType.REVIEW && records.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所选周期内还没有学习记录，请先记录一次学习再生成复盘");
        }

        AiReportResponse.SourceStats stats = calculateStats(records, tasks, periodStart, periodEnd);
        String evidenceJson = buildEvidenceJson(type, request, periodStart, periodEnd, goals, tasks, records, stats, user.getId());
        AiReportContent content = callDeepSeek(type, evidenceJson);

        AiReport report = new AiReport();
        report.setUserId(user.getId());
        report.setGoalId(selectedGoal == null ? null : selectedGoal.getId());
        report.setType(type);
        report.setTitle(content.title());
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setContentJson(writeJson(content));
        report.setModel(model);
        report.setRecordCount(stats.recordCount());
        report.setTotalMinutes(stats.totalMinutes());
        report.setCompletedTasks(stats.completedTasks());
        report.setAverageFocus(stats.averageFocus());
        report.setCreatedAt(LocalDateTime.now());
        reportMapper.insert(report);
        return new AiReportResponse(report.getId(), type, report.getGoalId(), periodStart, periodEnd,
                model, report.getCreatedAt(), stats, content);
    }

    public List<AiReportResponse> list(AiReportType type) {
        Long userId = currentUser.require().getId();
        return reportMapper.findByUserId(userId, type).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        AiReport report = reportMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "复盘报告不存在"));
        currentUser.requireAccess(report.getUserId());
        reportMapper.deleteById(id);
    }

    private void ensurePersonalizationEnabled(Long userId) {
        boolean enabled = preferenceMapper.findByUserId(userId)
                .map(UserPreference::getAiPersonalization)
                .orElse(Boolean.TRUE);
        if (!enabled) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先在个人中心开启 AI 个性化，再生成复盘或计划");
        }
    }

    private LearningGoal requireSelectedGoal(AiReportType type, Long goalId, Long userId) {
        if (type == AiReportType.PLAN && goalId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "生成学习计划前请选择一个学习目标");
        }
        if (goalId == null) return null;
        LearningGoal goal = goalMapper.findById(goalId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "学习目标不存在"));
        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权使用其他用户的学习目标");
        }
        return goal;
    }

    private AiReportResponse.SourceStats calculateStats(List<StudyRecord> records, List<LearningTask> tasks,
                                                         LocalDate start, LocalDate end) {
        int totalMinutes = records.stream().map(StudyRecord::getDurationMinutes)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        List<Integer> scores = records.stream().map(StudyRecord::getFocusScore).filter(Objects::nonNull).toList();
        Integer averageFocus = scores.isEmpty() ? null
                : (int) Math.round(scores.stream().mapToInt(Integer::intValue).average().orElse(0));
        int completedTasks = (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .filter(task -> task.getCompletedAt() == null
                        || (!task.getCompletedAt().toLocalDate().isBefore(start)
                        && !task.getCompletedAt().toLocalDate().isAfter(end)))
                .count();
        return new AiReportResponse.SourceStats(records.size(), totalMinutes, completedTasks, averageFocus);
    }

    private String buildEvidenceJson(AiReportType type, AiGenerateRequest request,
                                     LocalDate start, LocalDate end,
                                     List<LearningGoal> goals, List<LearningTask> tasks,
                                     List<StudyRecord> records, AiReportResponse.SourceStats stats,
                                     Long userId) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("任务", type == AiReportType.REVIEW ? "生成学习复盘" : "生成下一阶段学习计划");
        evidence.put("分析周期", Map.of("开始", start, "结束", end, "天数", request.effectiveDays()));
        evidence.put("统计", stats);
        evidence.put("学习目标", goals.stream().limit(8).map(goal -> Map.of(
                "标题", goal.getTitle(),
                "说明", Objects.toString(goal.getDescription(), ""),
                "当前水平", Objects.toString(goal.getCurrentLevel(), ""),
                "每周目标小时", Objects.requireNonNullElse(goal.getWeeklyHours(), 0),
                "截止日期", goal.getTargetDate(),
                "状态", goal.getStatus()
        )).toList());
        evidence.put("任务记录", tasks.stream().limit(30).map(task -> Map.of(
                "标题", task.getTitle(),
                "状态", task.getStatus(),
                "优先级", task.getPriority(),
                "预计分钟", Objects.requireNonNullElse(task.getEstimatedMinutes(), 0),
                "截止日期", Objects.toString(task.getDueDate(), "")
        )).toList());
        evidence.put("学习记录", records.stream().limit(30).map(record -> Map.of(
                "日期", record.getStudyDate(),
                "时长分钟", record.getDurationMinutes(),
                "内容", record.getContent(),
                "个人反思", Objects.toString(record.getReflection(), ""),
                "专注度", Objects.requireNonNullElse(record.getFocusScore(), 0)
        )).toList());
        preferenceMapper.findByUserId(userId).ifPresent(preference -> evidence.put("学习偏好", Map.of(
                "每周目标小时", preference.getWeeklyTargetHours(),
                "单次偏好分钟", preference.getPreferredSessionMinutes(),
                "偏好时段", preference.getPreferredStudyPeriod()
        )));
        if (request.note() != null && !request.note().isBlank()) {
            evidence.put("用户补充说明", request.note().trim());
        }
        return writeJson(evidence);
    }

    private AiReportContent callDeepSeek(AiReportType type, String evidenceJson) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "DeepSeek API 尚未配置，请设置 DEEPSEEK_API_KEY 后重启后端");
        }
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "请根据以下 JSON 学习证据生成"
                                + (type == AiReportType.REVIEW ? "复盘" : "计划") + "：\n" + evidenceJson)
                ),
                "thinking", Map.of("type", "disabled"),
                "response_format", Map.of("type", "json_object"),
                "max_tokens", 3000,
                "temperature", 0.3
        );
        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
            String rawContent = response == null ? null
                    : response.path("choices").path(0).path("message").path("content").asText(null);
            if (rawContent == null || rawContent.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "DeepSeek 未返回有效内容，请稍后重试");
            }
            return parseModelContent(rawContent);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.warn("DeepSeek request failed with status {}", exception.getStatusCode().value());
            String message = exception.getStatusCode().value() == 401
                    ? "DeepSeek API 密钥无效或已失效"
                    : exception.getStatusCode().value() == 429
                    ? "DeepSeek 请求过于频繁或账户余额不足，请稍后重试"
                    : "DeepSeek 服务暂时无法完成生成，请稍后重试";
            throw new BusinessException(HttpStatus.BAD_GATEWAY, message);
        } catch (RestClientException exception) {
            log.warn("DeepSeek request failed: {}", exception.getClass().getSimpleName());
            throw new BusinessException(HttpStatus.GATEWAY_TIMEOUT, "连接 DeepSeek 超时，请稍后重试");
        } catch (JsonProcessingException exception) {
            log.warn("DeepSeek returned invalid structured content");
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "DeepSeek 返回格式不完整，请重新生成");
        }
    }

    private AiReportContent parseModelContent(String rawContent) throws JsonProcessingException {
        String cleaned = rawContent.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        int objectStart = cleaned.indexOf('{');
        int objectEnd = cleaned.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            cleaned = cleaned.substring(objectStart, objectEnd + 1);
        }

        JsonNode root = objectMapper.readTree(cleaned);
        List<String> evidence = new ArrayList<>();
        JsonNode evidenceNode = root.path("evidence");
        if (evidenceNode.isArray()) {
            evidenceNode.forEach(node -> evidence.add(node.isTextual() ? node.asText() : node.toString()));
        }

        List<AiReportContent.AiInsight> insights = new ArrayList<>();
        JsonNode insightsNode = root.path("insights");
        if (insightsNode.isArray()) {
            insightsNode.forEach(node -> insights.add(new AiReportContent.AiInsight(
                    firstText(node, "title", "name"), firstText(node, "detail", "description", "reason"))));
        }

        List<AiReportContent.AiAction> actions = new ArrayList<>();
        JsonNode actionsNode = root.path("actions");
        if (actionsNode.isArray()) {
            actionsNode.forEach(node -> actions.add(new AiReportContent.AiAction(
                    firstText(node, "title", "name"),
                    firstText(node, "rationale", "reason", "description"),
                    firstInt(node, 45, "durationMinutes", "duration_minutes", "minutes"),
                    firstText(node, "priority", "level").toUpperCase())));
        }

        return normalize(new AiReportContent(
                firstText(root, "title", "reportTitle", "report_title"),
                firstText(root, "summary", "overview", "conclusion"),
                evidence, insights, actions));
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (value.isValueNode() && !value.asText().isBlank()) return value.asText();
        }
        return "";
    }

    private int firstInt(JsonNode node, int fallback, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (value.canConvertToInt()) return value.asInt();
        }
        return fallback;
    }

    private AiReportContent normalize(AiReportContent content) {
        if (content == null || content.title() == null || content.title().isBlank()
                || content.summary() == null || content.summary().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "DeepSeek 返回内容不完整，请重新生成");
        }
        List<String> evidence = content.evidence() == null ? List.of()
                : content.evidence().stream().filter(Objects::nonNull).filter(value -> !value.isBlank()).limit(4).toList();
        List<AiReportContent.AiInsight> insights = content.insights() == null ? List.of()
                : content.insights().stream().filter(Objects::nonNull).limit(4).toList();
        List<AiReportContent.AiAction> actions = content.actions() == null ? List.of()
                : content.actions().stream().filter(Objects::nonNull).limit(5).map(action -> {
                    int duration = action.durationMinutes() == null ? 45
                            : Math.max(15, Math.min(180, action.durationMinutes()));
                    String priority = List.of("HIGH", "MEDIUM", "LOW").contains(action.priority())
                            ? action.priority() : "MEDIUM";
                    return new AiReportContent.AiAction(action.title(), action.rationale(), duration, priority);
                }).toList();
        return new AiReportContent(content.title(), content.summary(), evidence, insights, actions);
    }

    private AiReportResponse toResponse(AiReport report) {
        try {
            AiReportContent content = objectMapper.readValue(report.getContentJson(), AiReportContent.class);
            AiReportResponse.SourceStats stats = new AiReportResponse.SourceStats(
                    Objects.requireNonNullElse(report.getRecordCount(), 0),
                    Objects.requireNonNullElse(report.getTotalMinutes(), 0),
                    Objects.requireNonNullElse(report.getCompletedTasks(), 0),
                    report.getAverageFocus());
            return new AiReportResponse(report.getId(), report.getType(), report.getGoalId(),
                    report.getPeriodStart(), report.getPeriodEnd(), report.getModel(), report.getCreatedAt(),
                    stats, content);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "历史报告内容无法读取");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "学习数据整理失败");
        }
    }
}
