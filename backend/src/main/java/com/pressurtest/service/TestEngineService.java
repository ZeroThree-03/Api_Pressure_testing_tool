package com.pressurtest.service;

import com.pressurtest.engine.ResultCollector;
import com.pressurtest.engine.TestEngine;
import com.pressurtest.engine.TestEngine.TestConfig;
import com.pressurtest.model.TestTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TestEngineService {

    private final TestEngine testEngine;

    public TestTask startTest(TestConfig config) {
        if (testEngine.isRunning()) {
            throw new IllegalStateException("已有测试正在运行");
        }

        TestTask task = new TestTask();
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now());
        task.setConfig(config.toString());

        CompletableFuture.runAsync(() -> {
            try {
                ResultCollector collector = testEngine.startTest(config);
                task.setStatus("completed");
                task.setCompletedAt(LocalDateTime.now());
                task.setResultSummary(collector.getMonitorData(0).toString());
            } catch (Exception e) {
                task.setStatus("failed");
                task.setCompletedAt(LocalDateTime.now());
            }
        });

        return task;
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
        long duration = 0;
        return collector.getMonitorData(duration);
    }
}
