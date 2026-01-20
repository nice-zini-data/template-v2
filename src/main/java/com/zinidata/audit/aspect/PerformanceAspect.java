package com.zinidata.audit.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 성능 모니터링 AOP
 * API 응답 시간, 메서드 실행 시간, 메모리 사용량 등을 측정하고 로깅
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    /**
     * Controller 메서드 성능 측정
     * API 응답 시간과 메모리 사용량을 측정
     */
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object measureApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // 시작 시간 및 메모리 상태 기록
        long startTime = System.currentTimeMillis();
        MemoryUsage memoryBefore = memoryBean.getHeapMemoryUsage();
        
        // 요청 정보 수집
        String apiInfo = getApiInfo(joinPoint);
        String httpMethod = "UNKNOWN";
        String requestUri = "UNKNOWN";
        String clientIp = "UNKNOWN";
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                httpMethod = request.getMethod();
                requestUri = request.getRequestURI();
                clientIp = getClientIpAddress(request);
            }
        } catch (Exception e) {
            // 요청 컨텍스트를 가져올 수 없는 경우 무시
        }
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            // 실제 메서드 실행
            result = joinPoint.proceed();
            
        } catch (Exception e) {
            success = false;
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw e;
            
        } finally {
            // 종료 시간 및 메모리 상태 기록
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            MemoryUsage memoryAfter = memoryBean.getHeapMemoryUsage();
            
            // 메모리 사용량 계산 (MB 단위)
            long memoryUsed = (memoryAfter.getUsed() - memoryBefore.getUsed()) / (1024 * 1024);
            long memoryMax = memoryAfter.getMax() / (1024 * 1024);
            long memoryCurrent = memoryAfter.getUsed() / (1024 * 1024);
            
            // 성능 로그 기록
            if (success) {
                if (duration > 1000) { // 1초 이상 걸린 API는 WARN 레벨
                    log.warn("SLOW_API | Method: {} | URI: {} | Duration: {}ms | Memory: +{}MB (Current: {}MB/{}MB) | IP: {}", 
                            httpMethod, requestUri, duration, memoryUsed, memoryCurrent, memoryMax, clientIp);
                } else {
                    log.info("API_PERFORMANCE | Method: {} | URI: {} | Duration: {}ms | Memory: +{}MB (Current: {}MB/{}MB) | IP: {}", 
                            httpMethod, requestUri, duration, memoryUsed, memoryCurrent, memoryMax, clientIp);
                }
            } else {
                log.error("API_ERROR | Method: {} | URI: {} | Duration: {}ms | Error: {} | IP: {}", 
                        httpMethod, requestUri, duration, errorMessage, clientIp);
            }
        }
        
        return result;
    }
    
    /**
     * Service 메서드 성능 측정
     * 비즈니스 로직 실행 시간을 측정
     */
    @Around("execution(* com.zinidata.*.service.*.*(..))")
    public Object measureServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        
        long startTime = System.currentTimeMillis();
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            
        } catch (Exception e) {
            success = false;
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw e;
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (success) {
                if (duration > 5000) { // 5초 이상 걸린 서비스 메서드는 WARN 레벨
                    log.warn("SLOW_SERVICE | Class: {} | Method: {} | Duration: {}ms", 
                            serviceName, methodName, duration);
                } else if (duration > 1000) { // 1초 이상은 INFO 레벨
                    log.info("SERVICE_PERFORMANCE | Class: {} | Method: {} | Duration: {}ms", 
                            serviceName, methodName, duration);
                }
                // 1초 미만은 로깅하지 않음 (너무 많은 로그 방지)
            } else {
                log.error("SERVICE_ERROR | Class: {} | Method: {} | Duration: {}ms | Error: {}", 
                        serviceName, methodName, duration, errorMessage);
            }
        }
        
        return result;
    }
    
    /**
     * Repository 메서드 성능 측정
     * 데이터베이스 접근 성능을 측정
     */
    @Around("execution(* com.zinidata.*.mapper.*.*(..))")
    public Object measureRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        
        long startTime = System.currentTimeMillis();
        String mapperName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            
        } catch (Exception e) {
            success = false;
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw e;
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (success) {
                if (duration > 3000) { // 3초 이상 걸린 쿼리는 WARN 레벨
                    log.warn("SLOW_QUERY | Mapper: {} | Method: {} | Duration: {}ms", 
                            mapperName, methodName, duration);
                } else if (duration > 500) { // 500ms 이상은 INFO 레벨
                    log.info("QUERY_PERFORMANCE | Mapper: {} | Method: {} | Duration: {}ms", 
                            mapperName, methodName, duration);
                }
                // 500ms 미만은 로깅하지 않음
            } else {
                log.error("QUERY_ERROR | Mapper: {} | Method: {} | Duration: {}ms | Error: {}", 
                        mapperName, methodName, duration, errorMessage);
            }
        }
        
        return result;
    }
    
    /**
     * API 정보 추출
     */
    private String getApiInfo(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("X-Real-IP");
        }
        
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        
        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        
        return clientIp;
    }
} 