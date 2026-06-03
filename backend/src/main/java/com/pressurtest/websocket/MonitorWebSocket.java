package com.pressurtest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pressurtest.engine.ResultCollector;
import com.pressurtest.service.TestEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorWebSocket extends TextWebSocketHandler {

    private final TestEngineService testEngineService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledExecutorService> schedulers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String taskId = getTaskId(session);
        sessions.put(taskId, session);
        startMonitor(taskId);
        log.info("WebSocket连接建立: {}", taskId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String taskId = getTaskId(session);
        sessions.remove(taskId);
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
                ResultCollector.MonitorData data = testEngineService.getMonitorData();
                if (data != null) {
                    Map<String, Object> message = Map.of(
                            "type", "monitor",
                            "data", data
                    );
                    sendMessage(taskId, objectMapper.writeValueAsString(message));
                }
            } catch (Exception e) {
                log.error("发送监控数据失败", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
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

    public void sendResult(String taskId, Object result) {
        try {
            Map<String, Object> message = Map.of(
                    "type", "result",
                    "data", result
            );
            sendMessage(taskId, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("发送结果失败", e);
        }
    }
}
