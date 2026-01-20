package com.zinidata.security.ratelimit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zinidata.security.ratelimit.util.RedisKeyGenerator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * IP 차단 서비스 (보안 필터용)
 * Redis 기반으로 IP 주소 차단 상태 확인 (빠른 응답용)
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpBlockCheckService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyGenerator keyGenerator;
    
    private static final String BLOCKED_VALUE = "BLOCKED";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * IP 차단 상태 확인 (보안 필터에서 사용)
     * 
     * @param ip IP 주소
     * @return true: 차단됨, false: 차단되지 않음
     */
    public boolean isBlocked(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        try {
            String key = keyGenerator.generateIpBlockKey(ip);
            String value = redisTemplate.opsForValue().get(key);
            boolean blocked = BLOCKED_VALUE.equals(value);
            
            if (blocked) {
                log.debug("차단된 IP 접근 시도 - IP: {}", ip);
            }
            
            return blocked;
        } catch (Exception e) {
            log.error("IP 차단 상태 확인 실패 - IP: {}", ip, e);
            // Redis 오류 시 차단하지 않음 (서비스 연속성 우선)
            return false;
        }
    }
    
    /**
     * IP 차단 등록 (Redis 캐시 업데이트용)
     * 
     * @param ip IP 주소
     * @param reason 차단 사유
     * @param ttlHours 차단 지속 시간 (시간), null인 경우 영구 차단
     */
    public void blockIp(String ip, String reason, Long ttlHours) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP 주소가 필요합니다.");
        }
        
        try {
            String key = keyGenerator.generateIpBlockKey(ip);
            String blockedTime = LocalDateTime.now().format(DATE_FORMATTER);
            String value = String.format("%s|%s|%s", BLOCKED_VALUE, reason != null ? reason : "관리자차단", blockedTime);
            
            if (ttlHours != null && ttlHours > 0) {
                // 임시 차단 (TTL 설정)
                redisTemplate.opsForValue().set(key, value, Duration.ofHours(ttlHours));
                log.info("IP 임시 차단 등록 - IP: {}, 사유: {}, 기간: {}시간", ip, reason, ttlHours);
            } else {
                // 영구 차단
                redisTemplate.opsForValue().set(key, value);
                log.info("IP 영구 차단 등록 - IP: {}, 사유: {}", ip, reason);
            }
            
        } catch (Exception e) {
            log.error("IP 차단 등록 실패 - IP: {}, 사유: {}", ip, reason, e);
            throw new RuntimeException("IP 차단 등록에 실패했습니다.");
        }
    }
    
    /**
     * IP 차단 해제 (Redis 캐시 업데이트용)
     * 
     * @param ip IP 주소
     */
    public void unblockIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP 주소가 필요합니다.");
        }
        
        try {
            String key = keyGenerator.generateIpBlockKey(ip);
            Boolean deleted = redisTemplate.delete(key);
            
            if (Boolean.TRUE.equals(deleted)) {
                log.info("IP 차단 해제 완료 - IP: {}", ip);
            } else {
                log.warn("IP 차단 해제 시도 - 차단되지 않은 IP: {}", ip);
            }
            
        } catch (Exception e) {
            log.error("IP 차단 해제 실패 - IP: {}", ip, e);
            throw new RuntimeException("IP 차단 해제에 실패했습니다.");
        }
    }
    
    /**
     * IP 차단 정보 조회 (Redis 캐시에서)
     * 
     * @param ip IP 주소
     * @return 차단 정보 (차단되지 않은 경우 null)
     */
    public IpBlockInfo getBlockInfo(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return null;
        }
        
        try {
            String key = keyGenerator.generateIpBlockKey(ip);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null || !value.startsWith(BLOCKED_VALUE)) {
                return null;
            }
            
            String[] parts = value.split("\\|");
            if (parts.length >= 3) {
                String reason = parts[1];
                String blockedTime = parts[2];
                
                // TTL 조회
                Long ttl = redisTemplate.getExpire(key);
                
                return new IpBlockInfo(ip, reason, blockedTime, ttl);
            }
            
            return new IpBlockInfo(ip, "알 수 없음", "알 수 없음", null);
            
        } catch (Exception e) {
            log.error("IP 차단 정보 조회 실패 - IP: {}", ip, e);
            return null;
        }
    }
    
    /**
     * 모든 차단된 IP 목록 조회 (Redis 캐시에서)
     * 
     * @return 차단된 IP 목록
     */
    public Set<String> getBlockedIps() {
        try {
            String pattern = keyGenerator.getProjectCode() + ":ip:blocked:*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys == null || keys.isEmpty()) {
                return Set.of();
            }
            
            // 키에서 IP 부분만 추출
            String prefix = keyGenerator.getProjectCode() + ":ip:blocked:";
            return keys.stream()
                .map(key -> key.substring(prefix.length()))
                .collect(java.util.stream.Collectors.toSet());
                
        } catch (Exception e) {
            log.error("차단된 IP 목록 조회 실패", e);
            return Set.of();
        }
    }
    
    /**
     * 일괄 IP 차단
     * 
     * @param ips IP 주소 목록
     * @param reason 차단 사유
     * @param ttlHours 차단 지속 시간 (시간), null인 경우 영구 차단
     */
    public void blockIps(Set<String> ips, String reason, Long ttlHours) {
        if (ips == null || ips.isEmpty()) {
            return;
        }
        
        for (String ip : ips) {
            try {
                blockIp(ip, reason, ttlHours);
            } catch (Exception e) {
                log.error("일괄 IP 차단 중 오류 - IP: {}", ip, e);
            }
        }
        
        log.info("일괄 IP 차단 완료 - 대상: {}개, 사유: {}", ips.size(), reason);
    }
    
    /**
     * 일괄 IP 차단 해제
     * 
     * @param ips IP 주소 목록
     */
    public void unblockIps(Set<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return;
        }
        
        for (String ip : ips) {
            try {
                unblockIp(ip);
            } catch (Exception e) {
                log.error("일괄 IP 차단 해제 중 오류 - IP: {}", ip, e);
            }
        }
        
        log.info("일괄 IP 차단 해제 완료 - 대상: {}개", ips.size());
    }
    
    /**
     * IP 차단 정보 VO
     */
    public static class IpBlockInfo {
        private final String ip;
        private final String reason;
        private final String blockedTime;
        private final Long ttlSeconds;
        
        public IpBlockInfo(String ip, String reason, String blockedTime, Long ttlSeconds) {
            this.ip = ip;
            this.reason = reason;
            this.blockedTime = blockedTime;
            this.ttlSeconds = ttlSeconds;
        }
        
        public String getIp() { return ip; }
        public String getReason() { return reason; }
        public String getBlockedTime() { return blockedTime; }
        public Long getTtlSeconds() { return ttlSeconds; }
        public boolean isPermanent() { return ttlSeconds == null || ttlSeconds < 0; }
        
        @Override
        public String toString() {
            return String.format("IpBlockInfo{ip='%s', reason='%s', blockedTime='%s', ttl=%s}", 
                               ip, reason, blockedTime, ttlSeconds);
        }
    }
} 