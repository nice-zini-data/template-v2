package com.zinidata.security.ratelimit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zinidata.security.ratelimit.exception.RateLimitExceededException;
import com.zinidata.security.ratelimit.util.RedisKeyGenerator;

import java.time.Duration;

/**
 * 문자인증 발송 제한 서비스
 * 
 * <p>휴대폰 번호별로 1시간당 10회까지만 인증번호 발송을 허용합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertRateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyGenerator keyGenerator;
    
    private static final int MAX_CERT_REQUESTS_PER_HOUR = 10;
    private static final long TTL_HOURS = 1;
    
    /**
     * 인증번호 발송 가능 여부 체크 및 카운트 증가
     * 
     * @param mobileNo 휴대폰 번호
     * @throws RateLimitExceededException 제한 초과 시
     */
    public void checkAndIncrement(String mobileNo) {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            throw new IllegalArgumentException("휴대폰 번호가 필요합니다.");
        }
        
        String key = keyGenerator.generateCertLimitKey(mobileNo);
        
        try {
            // 현재 카운트 조회
            String currentCountStr = redisTemplate.opsForValue().get(key);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            log.debug("문자인증 Rate Limit 체크 - 휴대폰: {}, 현재 카운트: {}, 제한: {}", 
                     mobileNo, currentCount, MAX_CERT_REQUESTS_PER_HOUR);
            
            // 제한 초과 체크
            if (currentCount >= MAX_CERT_REQUESTS_PER_HOUR) {
                long resetTime = System.currentTimeMillis() + (TTL_HOURS * 3600000);
                log.warn("문자인증 Rate Limit 초과 - 휴대폰: {}, 카운트: {}/{}", 
                        mobileNo, currentCount, MAX_CERT_REQUESTS_PER_HOUR);
                
                throw new RateLimitExceededException(
                    "문자인증", mobileNo, currentCount, MAX_CERT_REQUESTS_PER_HOUR, resetTime);
            }
            
            // 카운트 증가
            Long newCount = redisTemplate.opsForValue().increment(key);
            
            // 첫 번째 요청인 경우 TTL 설정
            if (newCount == 1) {
                redisTemplate.expire(key, Duration.ofHours(TTL_HOURS));
                log.debug("문자인증 Rate Limit TTL 설정 - Key: {}, TTL: {}시간", key, TTL_HOURS);
            }
            
            log.info("문자인증 발송 허용 - 휴대폰: {}, 카운트: {}/{}", 
                    mobileNo, newCount, MAX_CERT_REQUESTS_PER_HOUR);
            
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("문자인증 Rate Limit 체크 실패 - 휴대폰: {}, 오류: {}", mobileNo, e.getMessage());
            // Redis 오류 시에도 발송 허용 (서비스 연속성 우선)
            log.warn("Redis 오류로 인해 문자인증 Rate Limit 체크를 건너뜁니다.");
        }
    }
    
    /**
     * 현재 발송 횟수 조회
     * 
     * @param mobileNo 휴대폰 번호
     * @return 현재 시간대 발송 횟수
     */
    public int getCurrentCount(String mobileNo) {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            return 0;
        }
        
        try {
            String key = keyGenerator.generateCertLimitKey(mobileNo);
            String countStr = redisTemplate.opsForValue().get(key);
            return countStr != null ? Integer.parseInt(countStr) : 0;
        } catch (Exception e) {
            log.error("문자인증 카운트 조회 실패 - 휴대폰: {}", mobileNo, e);
            return 0;
        }
    }
    
    /**
     * 남은 발송 가능 횟수 조회
     * 
     * @param mobileNo 휴대폰 번호
     * @return 남은 발송 가능 횟수
     */
    public int getRemainingCount(String mobileNo) {
        int currentCount = getCurrentCount(mobileNo);
        return Math.max(0, MAX_CERT_REQUESTS_PER_HOUR - currentCount);
    }
    
    /**
     * 발송 제한 여부 확인 (카운트 증가 없이)
     * 
     * @param mobileNo 휴대폰 번호
     * @return true: 발송 가능, false: 제한 초과
     */
    public boolean canSend(String mobileNo) {
        int currentCount = getCurrentCount(mobileNo);
        return currentCount < MAX_CERT_REQUESTS_PER_HOUR;
    }
    
    /**
     * 특정 휴대폰 번호의 제한 초기화 (관리자용)
     * 
     * @param mobileNo 휴대폰 번호
     */
    public void resetLimit(String mobileNo) {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            return;
        }
        
        try {
            String key = keyGenerator.generateCertLimitKey(mobileNo);
            redisTemplate.delete(key);
            log.info("문자인증 Rate Limit 초기화 완료 - 휴대폰: {}", mobileNo);
        } catch (Exception e) {
            log.error("문자인증 Rate Limit 초기화 실패 - 휴대폰: {}", mobileNo, e);
        }
    }
} 