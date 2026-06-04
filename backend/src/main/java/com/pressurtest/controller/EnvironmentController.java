package com.pressurtest.controller;

import com.pressurtest.model.Environment;
import com.pressurtest.service.EnvironmentService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    public Map<String, Object> list() {
        List<Environment> envs = environmentService.listAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", envs);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Environment env) {
        Environment created = environmentService.create(env);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", created);
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Environment env) {
        Environment updated = environmentService.update(id, env);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", updated);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        environmentService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }

    @PutMapping("/{id}/activate")
    public Map<String, Object> activate(@PathVariable Long id) {
        Environment activated = environmentService.activate(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", activated);
        return result;
    }
}
