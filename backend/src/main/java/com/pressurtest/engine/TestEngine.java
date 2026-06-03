package com.pressurtest.engine;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class TestEngine {

    private final ThreadManager threadManager;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ResultCollector currentCollector;

    public ResultCollector startTest(TestConfig config) {
        if (running.get()) {
            throw new IllegalStateException("已有测试正在运行");
        }

        running.set(true);
        threadManager.setRunning(true);
        threadManager.init(config.getThreadCount());

        currentCollector = new ResultCollector();

        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < config.getThreadCount(); i++) {
            WorkerThread worker = new WorkerThread(
                    config.getMethod(),
                    config.getUrl(),
                    config.getHeaders(),
                    config.getBody(),
                    currentCollector,
                    running,
                    config.getLoopCount(),
                    config.getThinkTimeMin(),
                    config.getThinkTimeMax(),
                    config.getRetryCount()
            );

            if (config.getStartDelay() > 0) {
                try {
                    Thread.sleep(config.getStartDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            futures.add(threadManager.submit(worker));
        }

        if (config.getDuration() > 0) {
            Thread durationThread = new Thread(() -> {
                try {
                    Thread.sleep(config.getDuration() * 1000L);
                    stopTest();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            durationThread.start();
        }

        return currentCollector;
    }

    public void stopTest() {
        running.set(false);
        threadManager.setRunning(false);
        threadManager.shutdown();
    }

    public boolean isRunning() {
        return running.get();
    }

    public ResultCollector getCurrentCollector() {
        return currentCollector;
    }

    @Data
    public static class TestConfig {
        private String method;
        private String url;
        private Map<String, String> headers;
        private String body;
        private int threadCount = 10;
        private int loopCount = 1;
        private long duration = 0;
        private long startDelay = 0;
        private long thinkTimeMin = 0;
        private long thinkTimeMax = 0;
        private int timeout = 30;
        private int retryCount = 3;
    }
}
