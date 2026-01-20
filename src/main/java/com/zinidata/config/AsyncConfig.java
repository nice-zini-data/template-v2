package com.zinidata.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * 
 * <p>감사 로그 등의 비동기 작업을 위한 스레드 풀 설정을 제공합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    /**
     * 감사 로그 전용 스레드 풀 설정
     * 
     * <p>감사 로그 저장은 성능에 민감하므로 별도의 스레드 풀에서 처리합니다.</p>
     * 
     * @return 감사 로그 전용 executor
     */
    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 기본 스레드 수
        executor.setCorePoolSize(2);
        
        // 최대 스레드 수
        executor.setMaxPoolSize(5);
        
        // 큐 용량 (대기 작업 수)
        executor.setQueueCapacity(100);
        
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("AuditLog-");
        
        // 스레드 풀 종료 시 대기 시간 (초)
        executor.setAwaitTerminationSeconds(60);
        
        // 스레드 풀 종료 시 남은 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 거부 정책: CallerRunsPolicy (메인 스레드에서 실행)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        log.info("감사 로그 전용 스레드 풀 설정 완료: core={}, max={}, queue={}", 
                 executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 일반 비동기 작업용 기본 executor
     * 
     * @return 기본 executor
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Async-");
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        executor.initialize();
        
        log.info("기본 비동기 스레드 풀 설정 완료: core={}, max={}, queue={}", 
                 executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 비동기 작업 중 예외 처리
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 작업 실행 중 예외 발생: method={}, params={}, exception={}", 
                     method.getName(), params, ex.getMessage(), ex);
        };
    }
}