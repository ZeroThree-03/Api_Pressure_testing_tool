package com.pressurtest.engine;

import com.pressurtest.model.TestResult;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class WorkerThread implements Callable<Void> {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final ResultCollector collector;
    private final AtomicBoolean running;
    private final int loopCount;
    private final long thinkTimeMin;
    private final long thinkTimeMax;

    @Override
    public Void call() throws Exception {
        collector.incrementActiveThreads();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            int count = 0;
            while (running.get() && (loopCount == -1 || count < loopCount)) {
                if (!running.get()) break;

                TestResult result = executeRequest(httpClient);
                collector.addResult(result);
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

    private TestResult executeRequest(CloseableHttpClient httpClient) {
        TestResult result = new TestResult();
        result.setTimestamp(LocalDateTime.now());

        try {
            HttpUriRequestBase request = createRequest();
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

                if (response.getCode() >= 200 && response.getCode() < 300) {
                    result.setStatus("success");
                } else {
                    result.setStatus("failed");
                    result.setError("HTTP " + response.getCode());
                }

                if (response.getEntity() != null) {
                    result.setResponseBody(EntityUtils.toString(response.getEntity()));
                }

                return null;
            });

        } catch (Exception e) {
            result.setStatus("failed");
            result.setError(e.getMessage());
        }

        return result;
    }

    private HttpUriRequestBase createRequest() {
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
