package com.jeju_nongdi.jeju_nongdi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AsyncService {

    /**
     * @Async 어노테이션을 사용한 Virtual Thread 기반 비동기 처리
     */
    @Async("asyncExecutor")
    public CompletableFuture<String> processAsync(String data) {
        log.info("Processing data in virtual thread: {}, Thread: {}", 
                 data, Thread.currentThread());
        
        try {
            // 시뮬레이션을 위한 처리 시간
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
        
        return CompletableFuture.completedFuture("Processed: " + data);
    }

    /**
     * Virtual Thread Executor를 직접 사용한 비동기 처리
     */
    public CompletableFuture<String> processWithVirtualThread(String data) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Processing data in virtual thread: {}, Thread: {}", 
                     data, Thread.currentThread());
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }
            
            return "Processed with VT: " + data;
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * 대량 처리를 위한 Virtual Thread 활용
     */
    public CompletableFuture<Void> processBulkData(int count) {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        
        var futures = new CompletableFuture[count];
        for (int i = 0; i < count; i++) {
            final int index = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                log.info("Processing item {} in thread: {}", index, Thread.currentThread());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executor);
        }
        
        return CompletableFuture.allOf(futures);
    }
}
