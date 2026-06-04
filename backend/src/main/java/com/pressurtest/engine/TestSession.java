package com.pressurtest.engine;

import com.pressurtest.engine.TestEngine.TestConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
public class TestSession {

    private final String taskId;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ResultCollector collector = new ResultCollector();
    private volatile Thread durationThread;
    private volatile ExecutorService executor;
    private volatile long startTimeMillis;
    private volatile List<Future<Void>> futures;

    public TestSession(String taskId) {
        this.taskId = taskId;
    }

    public void start(TestConfig config) {
        if (running.get()) {
            throw new IllegalStateException("任务已在运行中");
        }

        if (!config.hasSteps()) {
            if (config.getUrl() == null || config.getUrl().isBlank()) {
                throw new IllegalArgumentException("URL不能为空");
            }
            if (config.getMethod() == null || config.getMethod().isBlank()) {
                throw new IllegalArgumentException("HTTP方法不能为空");
            }
        }
        if (config.getThreadCount() <= 0 || config.getThreadCount() > 1000) {
            throw new IllegalArgumentException("线程数必须在1-1000之间");
        }

        running.set(true);
        startTimeMillis = System.currentTimeMillis();
        executor = Executors.newFixedThreadPool(config.getThreadCount());

        futures = new ArrayList<>();
        for (int i = 0; i < config.getThreadCount(); i++) {
            WorkerThread worker = new WorkerThread(
                    config.getMethod(),
                    config.getUrl(),
                    config.getHeaders(),
                    config.getBody(),
                    config.getSteps(),
                    collector,
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

            futures.add(executor.submit(worker));
        }

        if (config.getDuration() > 0) {
            durationThread = new Thread(() -> {
                try {
                    Thread.sleep(config.getDuration() * 1000L);
                    stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            durationThread.setDaemon(true);
            durationThread.start();
        }
    }

    /**
     * 等待所有 worker 完成
     */
    public void waitForCompletion() {
        if (futures == null) return;
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                log.warn("Worker execution error: {}", e.getCause().getMessage());
            }
        }
        running.set(false);
    }

    public void stop() {
        running.set(false);
        if (durationThread != null) {
            durationThread.interrupt();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public long getDurationSeconds() {
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        return Math.max(1, elapsed / 1000);
    }

    public ResultCollector.MonitorData getMonitorData() {
        return collector.getMonitorData(getDurationSeconds());
    }
}
