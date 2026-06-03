package com.pressurtest.controller;

import com.pressurtest.service.CurlParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/curl")
@RequiredArgsConstructor
public class CurlController {

    private final CurlParserService curlParserService;

    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parse(@RequestBody Map<String, Object> request) {
        String curl = (String) request.get("curl");
        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) request.get("variables");

        Map<String, Object> result;
        if (variables != null && !variables.isEmpty()) {
            result = curlParserService.parseWithVariables(curl, variables);
        } else {
            result = curlParserService.parse(curl);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", result);

        return ResponseEntity.ok(response);
    }
}
