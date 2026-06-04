package com.pressurtest.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pressurtest.engine.TestEngine.RequestStep;
import com.pressurtest.model.TestResult;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerThread implements Callable<Void> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final List<RequestStep> steps;
    private final ResultCollector collector;
    private final AtomicBoolean running;
    private final int loopCount;
    private final long thinkTimeMin;
    private final long thinkTimeMax;
    private final int retryCount;

    public WorkerThread(String method, String url, Map<String, String> headers, String body,
                        ResultCollector collector, AtomicBoolean running,
                        int loopCount, long thinkTimeMin, long thinkTimeMax, int retryCount) {
        this(method, url, headers, body, null, collector, running, loopCount, thinkTimeMin, thinkTimeMax, retryCount);
    }

    public WorkerThread(String method, String url, Map<String, String> headers, String body,
                        List<RequestStep> steps,
                        ResultCollector collector, AtomicBoolean running,
                        int loopCount, long thinkTimeMin, long thinkTimeMax, int retryCount) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.steps = steps;
        this.collector = collector;
        this.running = running;
        this.loopCount = loopCount;
        this.thinkTimeMin = thinkTimeMin;
        this.thinkTimeMax = thinkTimeMax;
        this.retryCount = retryCount;
    }

    @Override
    public Void call() throws Exception {
        collector.incrementActiveThreads();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            int count = 0;
            while (running.get() && (loopCount == -1 || count < loopCount)) {
                if (!running.get()) break;

                if (steps != null && !steps.isEmpty()) {
                    for (RequestStep step : steps) {
                        if (!running.get()) break;
                        TestResult result = executeWithRetry(httpClient, step.getMethod(), step.getUrl(), step.getHeaders(), step.getBody());
                        collector.addResult(result);
                    }
                } else {
                    TestResult result = executeWithRetry(httpClient, method, url, headers, body);
                    collector.addResult(result);
                }
                count++;

                if (thinkTimeMax > 0) {
                    long thinkTime = thinkTimeMin + (long) (Math.random() * (thinkTimeMax - thinkTimeMin));
                    Thread.sleep(thinkTime);
                }
            }
        } finally {
            collector.decrementActiveThreads();
        }

        return null;
    }

    private TestResult executeWithRetry(CloseableHttpClient httpClient,
                                        String method, String url, Map<String, String> headers, String body) {
        TestResult result = null;
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            result = executeRequest(httpClient, method, url, headers, body);
            if ("success".equals(result.getStatus()) || attempt == retryCount) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return result;
    }

    private TestResult executeRequest(CloseableHttpClient httpClient,
                                      String method, String url, Map<String, String> headers, String body) {
        TestResult result = new TestResult();
        result.setTimestamp(LocalDateTime.now());

        try {
            HttpUriRequestBase request = createRequest(method, url, body);
            result.setMethod(method);
            result.setUrl(url);
            result.setRequestBody(body);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }

            long startTime = System.currentTimeMillis();
            httpClient.execute(request, response -> {
                long endTime = System.currentTimeMillis();
                result.setResponseTime(endTime - startTime);
                result.setStatusCode(response.getCode());

                if (response.getEntity() != null) {
                    result.setResponseBody(EntityUtils.toString(response.getEntity()));
                }

                // HTTP 状态码判断
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    result.setStatus("success");
                    // 响应体断言：检查 code 字段
                    assertResponseBody(result);
                } else {
                    result.setStatus("failed");
                    result.setError("HTTP " + response.getCode());
                }

                return null;
            });

        } catch (Exception e) {
            result.setStatus("failed");
            result.setError(e.getMessage());
        }

        return result;
    }

    private void assertResponseBody(TestResult result) {
        String responseBody = result.getResponseBody();
        if (responseBody == null || responseBody.isBlank()) return;

        try {
            JsonNode json = objectMapper.readTree(responseBody);
            if (json.has("code")) {
                int code = json.get("code").asInt();
                if (code != 200) {
                    result.setStatus("failed");
                    String msg = json.has("msg") ? json.get("msg").asText() : "";
                    result.setError("业务码: " + code + (msg.isEmpty() ? "" : " - " + msg));
                }
            }
        } catch (Exception ignored) {
            // 非 JSON 响应体，不做断言
        }
    }

    private HttpUriRequestBase createRequest(String method, String url, String body) {
        return switch (method.toUpperCase()) {
            case "GET" -> new HttpGet(url);
            case "POST" -> {
                HttpPost post = new HttpPost(url);
                if (body != null && !body.isEmpty()) {
                    post.setEntity(new StringEntity(body));
                }
                yield post;
            }
            case "PUT" -> {
                HttpPut put = new HttpPut(url);
                if (body != null && !body.isEmpty()) {
                    put.setEntity(new StringEntity(body));
                }
                yield put;
            }
            case "DELETE" -> new HttpDelete(url);
            default -> throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        };
    }
}
