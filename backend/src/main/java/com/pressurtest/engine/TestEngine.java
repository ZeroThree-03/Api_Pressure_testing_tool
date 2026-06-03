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
    private volatile ResultCollector currentCollector;
    private volatile Thread durationThread;

    public ResultCollector startTest(TestConfig config) {
        if (running.get()) {
            throw new IllegalStateException("已有测试正在运行");
        }

        if (config.getUrl() == null || config.getUrl().isBlank()) {
            throw new IllegalArgumentException("URL不能为空");
        }
        if (config.getMethod() == null || config.getMethod().isBlank()) {
            throw new IllegalArgumentException("HTTP方法不能为空");
        }
        if (config.getThreadCount() <= 0 || config.getThreadCount() > 1000) {
            throw new IllegalArgumentException("线程数必须在1-1000之间");
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
            durationThread = new Thread(() -> {
                try {
                    Thread.sleep(config.getDuration() * 1000L);
                    stopTest();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            durationThread.setDaemon(true);
            durationThread.start();
        }

        return currentCollector;
    }

    public void stopTest() {
        running.set(false);
        if (durationThread != null) {
            durationThread.interrupt();
        }
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
