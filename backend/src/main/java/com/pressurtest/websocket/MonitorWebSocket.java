package com.pressurtest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pressurtest.engine.ResultCollector;
import com.pressurtest.model.TestResult;
import com.pressurtest.service.TestEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MonitorWebSocket extends TextWebSocketHandler {

    private final TestEngineService testEngineService;
    private final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledExecutorService> schedulers = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> lastResultIndex = new ConcurrentHashMap<>();
    private final Set<String> completedTasks = ConcurrentHashMap.newKeySet();

    public MonitorWebSocket(TestEngineService testEngineService) {
        this.testEngineService = testEngineService;
    }

    @PostConstruct
    public void init() {
        // 注册任务完成监听器，发送最终数据
        testEngineService.onTaskComplete(taskId -> {
            sendFinalData(taskId);
            stopMonitor(taskId);
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String taskId = getTaskId(session);
            log.info("WebSocket连接建立, taskId={}, session={}, uri={}", taskId, session.getId(), session.getUri());
            sessions.put(taskId, session);
            lastResultIndex.put(taskId, new AtomicInteger(0));

            // 立即发送当前监控数据
            sendMonitorData(taskId);
            log.info("已发送监控数据, taskId={}", taskId);

            // 立即发送已有结果
            sendNewResults(taskId);
            log.info("已发送结果数据, taskId={}", taskId);

            if (!testEngineService.isTaskRunning(taskId)) {
                log.info("任务已完成, 发送最终数据, taskId={}", taskId);
                completedTasks.remove(taskId);
                sendFinalData(taskId);
            } else {
                log.info("任务运行中, 启动监控调度器, taskId={}", taskId);
                startMonitor(taskId);
            }
        } catch (Exception e) {
            log.error("WebSocket连接建立处理失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String taskId = getTaskId(session);
        sessions.remove(taskId);
        lastResultIndex.remove(taskId);
        completedTasks.remove(taskId);
        stopMonitor(taskId);
        log.info("WebSocket连接关闭: {}", taskId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端消息
    }

    private String getTaskId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            throw new IllegalStateException("WebSocket session has no URI");
        }
        String path = uri.toString();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void startMonitor(String taskId) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        schedulers.put(taskId, scheduler);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 任务完成后发送最终数据并停止调度器
                if (!testEngineService.isTaskRunning(taskId)) {
                    sendFinalData(taskId);
                    stopMonitor(taskId);
                    return;
                }
                sendMonitorData(taskId);
                sendNewResults(taskId);
            } catch (Exception e) {
                log.error("发送监控数据失败", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void sendMonitorData(String taskId) {
        ResultCollector.MonitorData data = testEngineService.getMonitorData(taskId);
        if (data != null) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "monitor",
                        "data", data
                );
                sendMessage(taskId, objectMapper.writeValueAsString(message));
            } catch (Exception e) {
                log.error("序列化监控数据失败", e);
            }
        }
    }

    private void sendNewResults(String taskId) {
        List<TestResult> recentResults = testEngineService.getRecentResults(taskId, 10000);
        if (recentResults == null || recentResults.isEmpty()) return;

        AtomicInteger lastIndex = lastResultIndex.get(taskId);
        if (lastIndex == null) return;

        int currentSize = recentResults.size();
        int lastIdx = lastIndex.get();
        if (currentSize > lastIdx) {
            List<TestResult> newResults = recentResults.subList(lastIdx, currentSize);
            try {
                Map<String, Object> resultMsg = Map.of(
                        "type", "result",
                        "data", newResults
                );
                sendMessage(taskId, objectMapper.writeValueAsString(resultMsg));
                lastIndex.set(currentSize);
            } catch (Exception e) {
                log.error("序列化结果数据失败", e);
            }
        }
    }

    /**
     * 任务完成时发送最终数据（幂等，只发送一次）
     */
    private void sendFinalData(String taskId) {
        if (!completedTasks.add(taskId)) {
            return; // 已经发送过
        }
        try {
            // 先发送所有剩余结果
            sendNewResults(taskId);
            // 发送最终监控数据
            sendMonitorData(taskId);
            // 发送完成状态（放在最后，确保不被覆盖）
            Map<String, Object> completeMsg = Map.of(
                    "type", "status",
                    "data", testEngineService.getTaskStatus(taskId).getStatus()
            );
            sendMessage(taskId, objectMapper.writeValueAsString(completeMsg));
            log.info("任务 {} 完成，已发送最终数据", taskId);
            // 停止调度器
            stopMonitor(taskId);
        } catch (Exception e) {
            log.error("发送最终数据失败", e);
        }
    }

    private void stopMonitor(String taskId) {
        ScheduledExecutorService scheduler = schedulers.remove(taskId);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void sendMessage(String taskId, String message) {
        WebSocketSession session = sessions.get(taskId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败", e);
            }
        }
    }
}
