package com.zinidata.domain.common.admin.service;

import com.zinidata.domain.common.admin.mapper.IpBlockMapper;
import com.zinidata.domain.common.admin.vo.IpBlockVO;
import com.zinidata.security.ratelimit.util.RedisKeyGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IP 차단 관리 서비스 (관리자용)
 * PostgreSQL DB 기반으로 IP 주소 차단/해제 관리 (Redis 캐시 병행)
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpBlockService {
    
    private final IpBlockMapper ipBlockMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyGenerator keyGenerator;
    
    @Value("${app.code:NBZM}")
    private String appCode;
    
    private static final String BLOCKED_VALUE = "BLOCKED";
    private static final String NOT_BLOCKED_VALUE = "NOT_BLOCKED";
    
    /**
     * IP 차단 상태 확인
     * 
     * @param ip IP 주소
     * @return 차단 여부
     */
    public boolean isBlocked(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 1. Redis 캐시 먼저 확인 (빠른 응답)
            String cacheKey = keyGenerator.generateIpBlockKey(ip);
            String cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (BLOCKED_VALUE.equals(cached)) {
                log.debug("차단된 IP 접근 시도 (캐시) - IP: {}", ip);
                return true;
            }
            if (NOT_BLOCKED_VALUE.equals(cached)) {
                return false;
            }

            // 2. DB에서 확인
            int blockCount = ipBlockMapper.existsActiveBlock(ip, appCode);
            boolean isBlocked = blockCount > 0;
            
            // 3. Redis 캐시 업데이트 (5분 TTL)
            redisTemplate.opsForValue().set(cacheKey, 
                isBlocked ? BLOCKED_VALUE : NOT_BLOCKED_VALUE, 
                Duration.ofMinutes(5));
            
            if (isBlocked) {
                log.debug("차단된 IP 접근 시도 (DB) - IP: {}", ip);
            }
            
            return isBlocked;
            
        } catch (Exception e) {
            log.error("IP 차단 상태 확인 실패 - IP: {}", ip, e);
            // DB/Redis 오류 시 서비스 연속성을 위해 차단되지 않은 것으로 처리
            return false;
        }
    }
    
    /**
     * IP 차단 등록
     * 
     * @param ip IP 주소
     * @param reason 차단 사유
     * @param ttlHours 차단 지속 시간 (시간), null인 경우 영구 차단
     */
    @Transactional
    public void blockIp(String ip, String reason, Long ttlHours) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP 주소가 필요합니다.");
        }
        
        try {
            // 1. 이미 활성 차단이 있는지 확인
            if (ipBlockMapper.existsActiveBlock(ip, appCode) > 0) {
                throw new IllegalStateException("이미 차단된 IP 주소입니다: " + ip);
            }

            // 2. 만료 시간 계산
            Timestamp expiresAt = null;
            if (ttlHours != null && ttlHours > 0) {
                expiresAt = new Timestamp(System.currentTimeMillis() + (ttlHours * 3600 * 1000));
            }

            // 3. DB에 차단 정보 저장
            IpBlockVO ipBlockVO = new IpBlockVO();
            ipBlockVO.setIpAddress(ip);
            ipBlockVO.setBlockReason(reason != null ? reason : "관리자차단");
            ipBlockVO.setIsPermanent(ttlHours == null);
            ipBlockVO.setExpiresAt(expiresAt);
            ipBlockVO.setAppCode(appCode);
            ipBlockVO.setBlockedBy(getCurrentUser());
            ipBlockVO.setBlockedAt(new Timestamp(System.currentTimeMillis()));
            ipBlockVO.setStatus("ACTIVE");
            
            ipBlockMapper.insertIpBlock(ipBlockVO);

            // 4. Redis 캐시 업데이트
            updateRedisCache(ip, true, ttlHours);
            
            if (ttlHours != null && ttlHours > 0) {
                log.info("IP 임시 차단 등록 - IP: {}, 사유: {}, 기간: {}시간", ip, reason, ttlHours);
            } else {
                log.info("IP 영구 차단 등록 - IP: {}, 사유: {}", ip, reason);
            }
            
        } catch (Exception e) {
            log.error("IP 차단 등록 실패 - IP: {}, 사유: {}", ip, reason, e);
            throw new RuntimeException("IP 차단 등록에 실패했습니다.", e);
        }
    }
    
    /**
     * IP 차단 해제
     * 
     * @param ip IP 주소
     */
    @Transactional
    public void unblockIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP 주소가 필요합니다.");
        }
        
        try {
            // 1. 활성 차단 조회
            IpBlockVO activeBlock = ipBlockMapper.findActiveBlock(ip, appCode);
            if (activeBlock == null) {
                log.warn("IP 차단 해제 시도 - 차단되지 않은 IP: {}", ip);
                return;
            }

            // 2. 차단 해제 처리
            int unblocked = ipBlockMapper.unblockIp(ip, appCode, getCurrentUser(), new Timestamp(System.currentTimeMillis()));

            // 3. Redis 캐시 업데이트
            updateRedisCache(ip, false, null);
            
            if (unblocked > 0) {
                log.info("IP 차단 해제 완료 - IP: {}", ip);
            } else {
                log.warn("IP 차단 해제 실패 - IP: {}", ip);
            }
            
        } catch (Exception e) {
            log.error("IP 차단 해제 실패 - IP: {}", ip, e);
            throw new RuntimeException("IP 차단 해제에 실패했습니다.", e);
        }
    }
    
    /**
     * 여러 IP 일괄 차단 해제
     * 
     * @param ips IP 주소 집합
     */
    @Transactional
    public void unblockIps(Set<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return;
        }
        
        try {
            // 1. DB에서 일괄 해제
            int unblocked = ipBlockMapper.unblockIpsBulk(
                ips, appCode, getCurrentUser(), new Timestamp(System.currentTimeMillis()));

            // 2. Redis 캐시 일괄 업데이트
            for (String ip : ips) {
                updateRedisCache(ip, false, null);
            }
            
            log.info("일괄 IP 차단 해제 완료 - 대상: {}개, 실제 해제: {}개", ips.size(), unblocked);
            
        } catch (Exception e) {
            log.error("일괄 IP 차단 해제 실패", e);
            throw new RuntimeException("일괄 IP 차단 해제에 실패했습니다.", e);
        }
    }
    
    /**
     * IP 차단 정보 조회
     * 
     * @param ip IP 주소
     * @return 차단 정보 (차단되지 않은 경우 null)
     */
    public IpBlockVO getBlockInfo(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return null;
        }
        
        try {
            return ipBlockMapper.findActiveBlock(ip, appCode);
        } catch (Exception e) {
            log.error("IP 차단 정보 조회 실패 - IP: {}", ip, e);
            return null;
        }
    }
    
    /**
     * 모든 차단된 IP 목록 조회
     * 
     * @return 차단된 IP 목록
     */
    public Set<String> getBlockedIps() {
        try {
            List<IpBlockVO> activeBlocks = ipBlockMapper.findAllActiveBlocks(appCode);
            return activeBlocks.stream()
                .filter(IpBlockVO::isActive)
                .map(IpBlockVO::getIpAddress)
                .collect(Collectors.toSet());
                
        } catch (Exception e) {
            log.error("차단된 IP 목록 조회 실패", e);
            return Set.of();
        }
    }

    /**
     * 프로젝트별 모든 차단 목록 조회 (관리자용)
     * 
     * @return 모든 차단 목록
     */
    public List<IpBlockVO> getAllBlocks() {
        try {
            return ipBlockMapper.findAllByProjectCode(appCode);
        } catch (Exception e) {
            log.error("모든 차단 목록 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 차단 통계 조회 (관리자용)
     * 
     * @return 차단 통계
     */
    public java.util.Map<String, Object> getBlockStatistics() {
        try {
            return ipBlockMapper.getBlockStatistics(appCode);
        } catch (Exception e) {
            log.error("차단 통계 조회 실패", e);
            return java.util.Map.of();
        }
    }

    /**
     * IP 패턴으로 검색 (관리자용)
     * 
     * @param ipPattern IP 패턴
     * @return 검색된 차단 목록
     */
    public List<IpBlockVO> searchByIpPattern(String ipPattern) {
        try {
            return ipBlockMapper.findByIpPattern("%" + ipPattern + "%", appCode);
        } catch (Exception e) {
            log.error("IP 패턴 검색 실패 - pattern: {}", ipPattern, e);
            return List.of();
        }
    }

    /**
     * 만료된 차단 정리 (스케줄러에서 호출)
     */
    @Transactional
    public void cleanupExpiredBlocks() {
        try {
            int expired = ipBlockMapper.updateExpiredBlocks(new Timestamp(System.currentTimeMillis()));
            if (expired > 0) {
                log.info("만료된 IP 차단 정리 완료 - {}개", expired);
            }
        } catch (Exception e) {
            log.error("만료된 IP 차단 정리 실패", e);
        }
    }

    /**
     * Redis 캐시 업데이트
     */
    private void updateRedisCache(String ip, boolean isBlocked, Long ttlHours) {
        try {
            String cacheKey = keyGenerator.generateIpBlockKey(ip);
            String cacheValue = isBlocked ? BLOCKED_VALUE : NOT_BLOCKED_VALUE;
            
            if (isBlocked && ttlHours != null) {
                // 임시 차단인 경우 TTL 설정
                redisTemplate.opsForValue().set(cacheKey, cacheValue, Duration.ofHours(ttlHours));
            } else {
                // 영구 차단이거나 해제인 경우 5분 캐시
                redisTemplate.opsForValue().set(cacheKey, cacheValue, Duration.ofMinutes(5));
            }
        } catch (Exception e) {
            log.warn("Redis 캐시 업데이트 실패 - IP: {}", ip, e);
        }
    }

    /**
     * 현재 사용자 정보 조회 (추후 Spring Security 연동)
     */
    private String getCurrentUser() {
        // TODO: Spring Security에서 현재 사용자 정보 조회
        return "admin"; // 임시값
    }
} 