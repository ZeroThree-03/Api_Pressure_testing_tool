package com.pressurtest.controller;

import com.pressurtest.model.RequestTemplate;
import com.pressurtest.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<RequestTemplate> templates = templateService.listAll();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", templates);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        RequestTemplate template = templateService.getById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", template);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody RequestTemplate template) {
        RequestTemplate created = templateService.create(template);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody RequestTemplate template) {
        RequestTemplate updated = templateService.update(id, template);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        templateService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
