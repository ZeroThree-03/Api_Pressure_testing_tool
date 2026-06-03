package com.pressurtest.service;

import com.pressurtest.engine.ResultCollector;
import com.pressurtest.engine.TestEngine;
import com.pressurtest.engine.TestEngine.TestConfig;
import com.pressurtest.model.TestTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestEngineService {

    private final TestEngine testEngine;
    private final ConcurrentHashMap<String, TestTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(0);
    private volatile long testStartTimeMillis = 0;

    public String startTest(TestConfig config) {
        if (testEngine.isRunning()) {
            throw new IllegalStateException("已有测试正在运行");
        }

        String taskId = String.valueOf(taskIdCounter.incrementAndGet());
        TestTask task = new TestTask();
        task.setId(Long.valueOf(taskId));
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now());
        task.setConfig(config.toString());
        tasks.put(taskId, task);

        testStartTimeMillis = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {
            try {
                ResultCollector collector = testEngine.startTest(config);
                task.setStatus("completed");
                task.setCompletedAt(LocalDateTime.now());
                long durationSeconds = Math.max(1, (System.currentTimeMillis() - testStartTimeMillis) / 1000);
                task.setResultSummary(collector.getMonitorData(durationSeconds).toString());
            } catch (Exception e) {
                log.error("Test execution failed", e);
                task.setStatus("failed");
                task.setCompletedAt(LocalDateTime.now());
                task.setResultSummary("Error: " + e.getMessage());
            }
        });

        return taskId;
    }

    public TestTask getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    public void stopTest() {
        testEngine.stopTest();
    }

    public boolean isRunning() {
        return testEngine.isRunning();
    }

    public ResultCollector.MonitorData getMonitorData() {
        ResultCollector collector = testEngine.getCurrentCollector();
        if (collector == null) {
            return null;
        }
        long durationSeconds = Math.max(1, (System.currentTimeMillis() - testStartTimeMillis) / 1000);
        return collector.getMonitorData(durationSeconds);
    }
}
