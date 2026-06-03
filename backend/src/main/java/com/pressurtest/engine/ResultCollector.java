package com.pressurtest.engine;

import com.pressurtest.model.TestResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class ResultCollector {
    private final ConcurrentLinkedQueue<TestResult> results = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0);

    public void addResult(TestResult result) {
        results.add(result);
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(result.getResponseTime());

        if ("success".equals(result.getStatus())) {
            successRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
    }

    public void incrementActiveThreads() {
        activeThreads.incrementAndGet();
    }

    public void decrementActiveThreads() {
        activeThreads.decrementAndGet();
    }

    public double getQps(long durationSeconds) {
        if (durationSeconds == 0) return 0;
        return (double) totalRequests.get() / durationSeconds;
    }

    public double getAvgResponseTime() {
        int total = totalRequests.get();
        if (total == 0) return 0;
        return (double) totalResponseTime.get() / total;
    }

    public double getErrorRate() {
        int total = totalRequests.get();
        if (total == 0) return 0;
        return (double) failedRequests.get() / total * 100;
    }

    public List<TestResult> getRecentResults(int count) {
        List<TestResult> allResults = new ArrayList<>(results);
        int size = allResults.size();
        if (size <= count) {
            return allResults;
        }
        return allResults.subList(size - count, size);
    }

    public MonitorData getMonitorData(long durationSeconds) {
        MonitorData data = new MonitorData();
        data.setQps(getQps(durationSeconds));
        data.setAvgResponseTime(getAvgResponseTime());
        data.setErrorRate(getErrorRate());
        data.setActiveThreads(activeThreads.get());
        data.setTotalRequests(totalRequests.get());
        data.setSuccessRequests(successRequests.get());
        data.setFailedRequests(failedRequests.get());
        return data;
    }

    @Data
    public static class MonitorData {
        private double qps;
        private double avgResponseTime;
        private double errorRate;
        private int activeThreads;
        private int totalRequests;
        private int successRequests;
        private int failedRequests;
    }
}
