package com.pressurtest.service;

import com.pressurtest.engine.ResultCollector;
import com.pressurtest.engine.TestEngine;
import com.pressurtest.engine.TestEngine.TestConfig;
import com.pressurtest.engine.TestSession;
import com.pressurtest.model.TestResult;
import com.pressurtest.model.TestTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
@Service
public class TestEngineService {

    private final ConcurrentHashMap<String, TestSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TestTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(0);
    private final List<Consumer<String>> taskCompleteListeners = new CopyOnWriteArrayList<>();

    public void onTaskComplete(Consumer<String> listener) {
        taskCompleteListeners.add(listener);
    }

    public String startTest(TestConfig config) {
        String taskId = String.valueOf(taskIdCounter.incrementAndGet());

        TestTask task = new TestTask();
        task.setId(Long.valueOf(taskId));
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now());
        task.setConfig(config.toString());
        tasks.put(taskId, task);

        TestSession session = new TestSession(taskId);
        sessions.put(taskId, session);

        CompletableFuture.runAsync(() -> {
            try {
                session.start(config);
                session.waitForCompletion();
                task.setStatus("completed");
                task.setCompletedAt(LocalDateTime.now());
                task.setResultSummary(session.getMonitorData().toString());
            } catch (Exception e) {
                log.error("Test execution failed for task {}", taskId, e);
                task.setStatus("failed");
                task.setCompletedAt(LocalDateTime.now());
                task.setResultSummary("Error: " + e.getMessage());
            } finally {
                // 通知监听器任务完成
                for (Consumer<String> listener : taskCompleteListeners) {
                    try {
                        listener.accept(taskId);
                    } catch (Exception e) {
                        log.error("Task complete listener error", e);
                    }
                }
            }
        });

        return taskId;
    }

    public void stopTest(String taskId) {
        TestSession session = sessions.get(taskId);
        if (session != null) {
            session.stop();
            TestTask task = tasks.get(taskId);
            if (task != null) {
                task.setStatus("stopped");
                task.setCompletedAt(LocalDateTime.now());
            }
        }
    }

    public TestTask getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    public List<TestTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public boolean isTaskRunning(String taskId) {
        TestSession session = sessions.get(taskId);
        return session != null && session.isRunning();
    }

    public ResultCollector.MonitorData getMonitorData(String taskId) {
        TestSession session = sessions.get(taskId);
        if (session == null) {
            return null;
        }
        return session.getMonitorData();
    }

    public List<TestResult> getRecentResults(String taskId, int count) {
        TestSession session = sessions.get(taskId);
        if (session == null) {
            return Collections.emptyList();
        }
        return session.getCollector().getRecentResults(count);
    }

    public void deleteTest(String taskId) {
        TestSession session = sessions.remove(taskId);
        if (session != null && session.isRunning()) {
            session.stop();
        }
        tasks.remove(taskId);
    }
}
