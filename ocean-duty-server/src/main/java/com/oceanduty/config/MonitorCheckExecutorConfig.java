package com.oceanduty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控检测并行执行线程池
 */
@Configuration
public class MonitorCheckExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService monitorCheckExecutor(
            @Value("${ocean-duty.monitor.check-parallelism:8}") int parallelism) {
        int poolSize = Math.max(2, parallelism);
        AtomicInteger threadIndex = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "monitor-check-" + threadIndex.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(poolSize, threadFactory);
    }
}
