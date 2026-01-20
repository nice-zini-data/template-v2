package com.zinidata.security.ratelimit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zinidata.security.ratelimit.exception.RateLimitExceededException;
import com.zinidata.security.ratelimit.util.RedisKeyGenerator;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * API 호출 제한 서비스
 * 
 * <p>특정 데이터 조회 API에 대해 IP별로 1시간당 제한된 횟수만 호출을 허용합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyGenerator keyGenerator;
    
    // 제한 대상 API 패턴 목록
    private static final List<String> RATE_LIMITED_APIS = Arrays.asList(
        "/api/report/aaa",
        "/api/report/bbb",
        "/api/data/export",
        "/api/data/download",
        "/api/analysis/premium"
    );
    
    private static final int MAX_API_REQUESTS_PER_HOUR = 30;
    private static final long TTL_HOURS = 1;
    
    /**
     * API 호출 가능 여부 체크 및 카운트 증가
     * 
     * @param clientIp 클라이언트 IP
     * @param endpoint API 엔드포인트
     * @throws RateLimitExceededException 제한 초과 시
     */
    public void checkAndIncrement(String clientIp, String endpoint) {
        if (!isRateLimitedApi(endpoint)) {
            return; // 제한 대상이 아닌 API는 통과
        }
        
        if (clientIp == null || clientIp.trim().isEmpty()) {
            throw new IllegalArgumentException("클라이언트 IP가 필요합니다.");
        }
        
        String key = keyGenerator.generateApiLimitKey(clientIp, endpoint);
        
        try {
            // 현재 카운트 조회
            String currentCountStr = redisTemplate.opsForValue().get(key);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            log.debug("API Rate Limit 체크 - IP: {}, API: {}, 현재 카운트: {}, 제한: {}", 
                     clientIp, endpoint, currentCount, MAX_API_REQUESTS_PER_HOUR);
            
            // 제한 초과 체크
            if (currentCount >= MAX_API_REQUESTS_PER_HOUR) {
                long resetTime = System.currentTimeMillis() + (TTL_HOURS * 3600000);
                log.warn("API Rate Limit 초과 - IP: {}, API: {}, 카운트: {}/{}", 
                        clientIp, endpoint, currentCount, MAX_API_REQUESTS_PER_HOUR);
                
                throw new RateLimitExceededException(
                    "API호출", clientIp + " -> " + endpoint, 
                    currentCount, MAX_API_REQUESTS_PER_HOUR, resetTime);
            }
            
            // 카운트 증가
            Long newCount = redisTemplate.opsForValue().increment(key);
            
            // 첫 번째 요청인 경우 TTL 설정
            if (newCount == 1) {
                redisTemplate.expire(key, Duration.ofHours(TTL_HOURS));
                log.debug("API Rate Limit TTL 설정 - Key: {}, TTL: {}시간", key, TTL_HOURS);
            }
            
            log.info("API 호출 허용 - IP: {}, API: {}, 카운트: {}/{}", 
                    clientIp, endpoint, newCount, MAX_API_REQUESTS_PER_HOUR);
            
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("API Rate Limit 체크 실패 - IP: {}, API: {}, 오류: {}", 
                     clientIp, endpoint, e.getMessage());
            // Redis 오류 시에도 API 호출 허용 (서비스 연속성 우선)
            log.warn("Redis 오류로 인해 API Rate Limit 체크를 건너뜁니다.");
        }
    }
    
    /**
     * 현재 API 호출 횟수 조회
     * 
     * @param clientIp 클라이언트 IP
     * @param endpoint API 엔드포인트
     * @return 현재 시간대 호출 횟수
     */
    public int getCurrentCount(String clientIp, String endpoint) {
        if (clientIp == null || clientIp.trim().isEmpty() || !isRateLimitedApi(endpoint)) {
            return 0;
        }
        
        try {
            String key = keyGenerator.generateApiLimitKey(clientIp, endpoint);
            String countStr = redisTemplate.opsForValue().get(key);
            return countStr != null ? Integer.parseInt(countStr) : 0;
        } catch (Exception e) {
            log.error("API 호출 카운트 조회 실패 - IP: {}, API: {}", clientIp, endpoint, e);
            return 0;
        }
    }
    
    /**
     * 남은 API 호출 가능 횟수 조회
     * 
     * @param clientIp 클라이언트 IP
     * @param endpoint API 엔드포인트
     * @return 남은 호출 가능 횟수
     */
    public int getRemainingCount(String clientIp, String endpoint) {
        int currentCount = getCurrentCount(clientIp, endpoint);
        return Math.max(0, MAX_API_REQUESTS_PER_HOUR - currentCount);
    }
    
    /**
     * API 호출 제한 여부 확인 (카운트 증가 없이)
     * 
     * @param clientIp 클라이언트 IP
     * @param endpoint API 엔드포인트
     * @return true: 호출 가능, false: 제한 초과
     */
    public boolean canCall(String clientIp, String endpoint) {
        if (!isRateLimitedApi(endpoint)) {
            return true; // 제한 대상이 아닌 API는 항상 허용
        }
        
        int currentCount = getCurrentCount(clientIp, endpoint);
        return currentCount < MAX_API_REQUESTS_PER_HOUR;
    }
    
    /**
     * Rate Limit 대상 API인지 확인
     * 
     * @param endpoint API 엔드포인트
     * @return true: 제한 대상, false: 제한 대상 아님
     */
    public boolean isRateLimitedApi(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return false;
        }
        
        return RATE_LIMITED_APIS.stream()
            .anyMatch(api -> endpoint.startsWith(api));
    }
    
    /**
     * 특정 IP의 API 제한 초기화 (관리자용)
     * 
     * @param clientIp 클라이언트 IP
     * @param endpoint API 엔드포인트 (null인 경우 모든 API 초기화)
     */
    public void resetLimit(String clientIp, String endpoint) {
        if (clientIp == null || clientIp.trim().isEmpty()) {
            return;
        }
        
        try {
            if (endpoint != null && !endpoint.trim().isEmpty()) {
                // 특정 API만 초기화
                String key = keyGenerator.generateApiLimitKey(clientIp, endpoint);
                redisTemplate.delete(key);
                log.info("API Rate Limit 초기화 완료 - IP: {}, API: {}", clientIp, endpoint);
            } else {
                // 해당 IP의 모든 API 제한 초기화
                for (String api : RATE_LIMITED_APIS) {
                    String key = keyGenerator.generateApiLimitKey(clientIp, api);
                    redisTemplate.delete(key);
                }
                log.info("API Rate Limit 전체 초기화 완료 - IP: {}", clientIp);
            }
        } catch (Exception e) {
            log.error("API Rate Limit 초기화 실패 - IP: {}, API: {}", clientIp, endpoint, e);
        }
    }
    
    /**
     * 제한 대상 API 목록 반환
     * 
     * @return 제한 대상 API 목록
     */
    public List<String> getRateLimitedApis() {
        return List.copyOf(RATE_LIMITED_APIS);
    }
} 