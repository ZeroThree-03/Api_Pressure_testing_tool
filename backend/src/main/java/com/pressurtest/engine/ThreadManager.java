package com.pressurtest.engine;

import lombok.Data;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class ThreadManager {

    private volatile ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void init(int threadCount) {
        shutdown();
        executor = Executors.newFixedThreadPool(threadCount);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void shutdown() {
        running.set(false);
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

    public void setRunning(boolean value) {
        running.set(value);
    }
}
