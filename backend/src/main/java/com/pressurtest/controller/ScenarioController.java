package com.pressurtest.controller;

import com.pressurtest.controller.dto.ScenarioRequest;
import com.pressurtest.model.ScenarioStep;
import com.pressurtest.model.TestScenario;
import com.pressurtest.service.ScenarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<TestScenario> scenarios = scenarioService.listAll();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", scenarios);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        TestScenario scenario = scenarioService.getById(id);
        if (scenario == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", -1);
            error.put("message", "场景不存在");
            return ResponseEntity.status(404).body(error);
        }

        List<ScenarioStep> steps = scenarioService.getSteps(id);

        Map<String, Object> data = new HashMap<>();
        data.put("scenario", scenario);
        data.put("steps", steps);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ScenarioRequest request) {
        TestScenario scenario = new TestScenario();
        scenario.setName(request.getName());
        scenario.setType(request.getType());
        scenario.setDescription(request.getDescription());
        scenario.setConfig(request.getConfig());

        TestScenario created = scenarioService.create(scenario, request.getSteps());
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody ScenarioRequest request) {
        TestScenario scenario = new TestScenario();
        scenario.setName(request.getName());
        scenario.setType(request.getType());
        scenario.setDescription(request.getDescription());
        scenario.setConfig(request.getConfig());

        TestScenario updated = scenarioService.update(id, scenario, request.getSteps());
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        scenarioService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
