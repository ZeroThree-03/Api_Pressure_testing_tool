package com.pressurtest.controller;

import com.pressurtest.model.EnvVariable;
import com.pressurtest.service.EnvVariableService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/env-variables")
public class EnvVariableController {

    private final EnvVariableService envVariableService;

    public EnvVariableController(EnvVariableService envVariableService) {
        this.envVariableService = envVariableService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam Long environmentId) {
        List<EnvVariable> variables = envVariableService.listByEnvironment(environmentId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", variables);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody EnvVariable variable) {
        EnvVariable created = envVariableService.create(variable);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", created);
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody EnvVariable variable) {
        EnvVariable updated = envVariableService.update(id, variable);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", updated);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        envVariableService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }

    @PostMapping("/batch")
    public Map<String, Object> batchCreate(@RequestBody Map<String, Object> body) {
        Long environmentId = Long.valueOf(body.get("environmentId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> varList = (List<Map<String, String>>) body.get("variables");

        List<EnvVariable> variables = varList.stream().map(v -> {
            EnvVariable ev = new EnvVariable();
            ev.setName(v.get("name"));
            ev.setValue(v.get("value"));
            ev.setDescription(v.getOrDefault("description", ""));
            return ev;
        }).toList();

        List<EnvVariable> result = envVariableService.batchCreate(environmentId, variables);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", result);
        return response;
    }
}
