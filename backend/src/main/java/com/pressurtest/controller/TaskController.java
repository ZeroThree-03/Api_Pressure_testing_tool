package com.pressurtest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pressurtest.engine.TestEngine.RequestStep;
import com.pressurtest.engine.TestEngine.TestConfig;
import com.pressurtest.model.RequestTemplate;
import com.pressurtest.model.ScenarioStep;
import com.pressurtest.model.TestScenario;
import com.pressurtest.model.TestTask;
import com.pressurtest.service.ScenarioService;
import com.pressurtest.service.TemplateService;
import com.pressurtest.service.TestEngineService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TestEngineService testEngineService;
    private final ScenarioService scenarioService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskController(TestEngineService testEngineService,
                          ScenarioService scenarioService,
                          TemplateService templateService) {
        this.testEngineService = testEngineService;
        this.scenarioService = scenarioService;
        this.templateService = templateService;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody Map<String, Object> body) {
        TestConfig config = new TestConfig();

        if (body.containsKey("scenarioId")) {
            Long scenarioId = Long.valueOf(body.get("scenarioId").toString());
            buildConfigFromScenario(config, scenarioId);
        } else {
            config.setMethod((String) body.getOrDefault("method", "GET"));
            config.setUrl((String) body.getOrDefault("url", ""));
            config.setBody((String) body.get("body"));
            config.setThreadCount(((Number) body.getOrDefault("threadCount", 10)).intValue());
            config.setDuration(((Number) body.getOrDefault("duration", 0)).longValue());
            config.setStartDelay(((Number) body.getOrDefault("startDelay", 0)).longValue());
            config.setThinkTimeMin(((Number) body.getOrDefault("thinkTimeMin", 0)).longValue());
            config.setThinkTimeMax(((Number) body.getOrDefault("thinkTimeMax", 0)).longValue());
            config.setRetryCount(((Number) body.getOrDefault("retryCount", 3)).intValue());
        }

        String taskId = testEngineService.startTest(config);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", taskId);
        return result;
    }

    private void buildConfigFromScenario(TestConfig config, Long scenarioId) {
        TestScenario scenario = scenarioService.getById(scenarioId);
        if (scenario == null) {
            throw new IllegalArgumentException("场景不存在");
        }

        // 从场景配置中读取压测参数
        if (scenario.getConfig() != null && !scenario.getConfig().isBlank()) {
            try {
                Map<String, Object> scenarioConfig = objectMapper.readValue(
                        scenario.getConfig(), new TypeReference<>() {});
                int concurrency = toInt(scenarioConfig.get("concurrency"), 10);
                long rampUp = toLong(scenarioConfig.get("rampUp"), 0);
                config.setThreadCount(concurrency);
                config.setDuration(toLong(scenarioConfig.get("duration"), 0));
                config.setStartDelay(rampUp > 0 && concurrency > 0 ? rampUp * 1000L / concurrency : 0);
                config.setLoopCount(toInt(scenarioConfig.get("loopCount"), 1));
            } catch (Exception e) {
                // 配置解析失败使用默认值
            }
        }

        // 加载所有步骤的模板作为请求配置
        List<ScenarioStep> steps = scenarioService.getSteps(scenarioId);
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("场景没有配置步骤");
        }

        List<RequestStep> requestSteps = new ArrayList<>();
        for (ScenarioStep step : steps) {
            if (step.getTemplateId() == null) continue;

            RequestTemplate template = templateService.getById(step.getTemplateId());
            if (template == null) continue;

            RequestStep rs = new RequestStep();
            rs.setMethod(template.getMethod());
            rs.setUrl(template.getUrl());
            rs.setBody(template.getBody());

            if (template.getHeaders() != null && !template.getHeaders().isBlank()) {
                try {
                    rs.setHeaders(objectMapper.readValue(template.getHeaders(), new TypeReference<>() {}));
                } catch (Exception e) {
                    // headers 解析失败忽略
                }
            }

            requestSteps.add(rs);
        }

        if (requestSteps.isEmpty()) {
            throw new IllegalArgumentException("场景没有有效的步骤");
        }

        config.setSteps(requestSteps);
        // 第一个步骤作为默认值（兼容单步骤模式）
        config.setMethod(requestSteps.get(0).getMethod());
        config.setUrl(requestSteps.get(0).getUrl());
        config.setBody(requestSteps.get(0).getBody());
        config.setHeaders(requestSteps.get(0).getHeaders());
    }

    private int toInt(Object value, int defaultVal) {
        if (value == null) return defaultVal;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return defaultVal; }
    }

    private long toLong(Object value, long defaultVal) {
        if (value == null) return defaultVal;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); } catch (Exception e) { return defaultVal; }
    }

    @PostMapping("/{taskId}/stop")
    public Map<String, Object> stop(@PathVariable String taskId) {
        testEngineService.stopTest(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "已停止");
        return result;
    }

    @GetMapping("/{taskId}/status")
    public Map<String, Object> status(@PathVariable String taskId) {
        TestTask task = testEngineService.getTaskStatus(taskId);
        Map<String, Object> result = new HashMap<>();
        if (task == null) {
            result.put("code", -1);
            result.put("message", "任务不存在");
        } else {
            result.put("code", 0);
            result.put("data", task);
        }
        return result;
    }

    @DeleteMapping("/{taskId}")
    public Map<String, Object> delete(@PathVariable String taskId) {
        testEngineService.deleteTest(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        return result;
    }

    @GetMapping
    public Map<String, Object> list() {
        List<TestTask> allTasks = testEngineService.getAllTasks();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", allTasks);
        return result;
    }
}
