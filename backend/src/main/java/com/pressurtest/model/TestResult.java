package com.pressurtest.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestResult {
    private Long id;
    private String status;
    private String method;
    private String url;
    private int statusCode;
    private long responseTime;
    private String requestBody;
    private String responseBody;
    private String error;
    private LocalDateTime timestamp;
}
