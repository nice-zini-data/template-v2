package com.zinidata.security.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Redis 기반 SessionRegistry 구현체
 * Spring Session Redis와 Spring Security SessionRegistry 연동
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionRegistry implements SessionRegistry {

    private final RedisTemplate<String, Object> redisTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${spring.application.name}")
    private String applicationName;
    
    // 메모리 기반 세션 정보 캐시 (성능 최적화)
    private final Map<Object, Set<String>> principalToSessionIds = new ConcurrentHashMap<>();
    private final Map<String, SessionInformation> sessionIdToSessionInfo = new ConcurrentHashMap<>();
    
    // Redis 키 PREFIX는 @EnableRedisHttpSession의 redisNamespace와 일치해야 함
    private static final String PRINCIPAL_SESSION_KEY = "auth:principal:sessions:";
    private static final String SPRING_SECURITY_CONTEXT_KEY = "sessionAttr:SPRING_SECURITY_CONTEXT";
    
    /**
     * 동적으로 Redis 세션 PREFIX 생성
     * SessionConfig의 redisNamespace와 일치: ${spring.application.name}:session:sessions:
     */
    private String getRedisSessionPrefix() {
        return applicationName + ":session:sessions:";
    }
    
    /**
     * 두 Principal 객체가 동일한지 비교
     */
    private boolean principalsMatch(Object principal1, Object principal2) {
        if (principal1 == null || principal2 == null) {
            return false;
        }
        
        // User 객체인 경우 username으로 비교
        if (principal1 instanceof User && principal2 instanceof User) {
            return ((User) principal1).getUsername().equals(((User) principal2).getUsername());
        }
        
        // 그 외는 equals로 비교
        return principal1.equals(principal2);
    }

    @Override
    public List<Object> getAllPrincipals() {
        log.debug("[REDIS-SESSION-REGISTRY] getAllPrincipals 호출 - 메모리 + Redis Principal 인덱스 통합 조회");
        
        // 1. 메모리에 있는 Principal
        Set<Object> allPrincipals = new HashSet<>(principalToSessionIds.keySet());
        
        // 2. Redis의 auth:principal:sessions:* 키에서 Principal 추출
        try {
            Set<String> principalKeys = redisTemplate.keys("auth:principal:sessions:*");
            if (principalKeys != null && !principalKeys.isEmpty()) {
                log.debug("[REDIS-SESSION-REGISTRY] Redis Principal 인덱스 키 개수: {}", principalKeys.size());
                
                for (String principalKey : principalKeys) {
                    try {
                        // Principal 문자열 추출: "auth:principal:sessions:CustomUserDetailsV1{...}" 
                        String principalStr = principalKey.replace("auth:principal:sessions:", "");
                        
                        // 이미 메모리에 있는 Principal과 일치하는지 확인
                        boolean found = false;
                        for (Object existingPrincipal : allPrincipals) {
                            if (existingPrincipal.toString().equals(principalStr)) {
                                found = true;
                                break;
                            }
                        }
                        
                        // 메모리에 없으면 새로 추가 (toString() 결과를 임시 Principal로 사용)
                        if (!found) {
                            // Principal 문자열을 그대로 저장 (UserDetails 객체 재구성은 복잡하므로)
                            allPrincipals.add(new SimplePrincipal(principalStr));
                            log.debug("[REDIS-SESSION-REGISTRY] Redis에서 Principal 추가: {}", principalStr);
                        }
                    } catch (Exception e) {
                        log.warn("[REDIS-SESSION-REGISTRY] Principal 처리 실패: {} - {}", principalKey, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[REDIS-SESSION-REGISTRY] Redis Principal 인덱스 조회 실패", e);
        }
        
        log.info("[REDIS-SESSION-REGISTRY] 총 Principal 수: {} (메모리+Redis 통합)", allPrincipals.size());
        return new ArrayList<>(allPrincipals);
    }
    
    /**
     * Principal 문자열을 임시로 저장하기 위한 간단한 클래스
     */
    private static class SimplePrincipal implements org.springframework.security.core.userdetails.UserDetails {
        private final String principalString;
        
        public SimplePrincipal(String principalString) {
            this.principalString = principalString;
        }
        
        @Override
        public String getUsername() {
            // "CustomUserDetailsV1{memNo=12901, loginId='dovmf3025', memNm='권성민'}" 에서 loginId 추출
            try {
                int start = principalString.indexOf("loginId='") + 9;
                int end = principalString.indexOf("'", start);
                return principalString.substring(start, end);
            } catch (Exception e) {
                return principalString;
            }
        }
        
        @Override
        public String toString() {
            return principalString;
        }
        
        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public String getPassword() {
            return null;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
    }
    
    /**
     * SecurityContext에서 Principal 추출
     */
    private Object extractPrincipalFromContext(Object contextObj) {
        try {
            // contextObj는 SecurityContext 객체
            if (contextObj instanceof org.springframework.security.core.context.SecurityContext) {
                org.springframework.security.core.context.SecurityContext context = 
                    (org.springframework.security.core.context.SecurityContext) contextObj;
                
                if (context.getAuthentication() != null) {
                    return context.getAuthentication().getPrincipal();
                }
            }
        } catch (Exception e) {
            log.warn("[REDIS-SESSION-REGISTRY] Principal 추출 실패", e);
        }
        return null;
    }

    @Override
    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions) {
        log.debug("[REDIS-SESSION-REGISTRY] getAllSessions 호출 - principal: {}", principal);
        
        List<SessionInformation> sessions = new ArrayList<>();
        
        // 1️⃣ 먼저 인메모리 캐시 확인
        Set<String> sessionIds = principalToSessionIds.get(principal);
        
        if (sessionIds != null && !sessionIds.isEmpty()) {
            log.debug("[REDIS-SESSION-REGISTRY] 메모리에서 찾은 세션ID 목록: {}", sessionIds);
            for (String sessionId : sessionIds) {
                SessionInformation sessionInfo = sessionIdToSessionInfo.get(sessionId);
                if (sessionInfo != null) {
                    // Redis에서 실제 세션 존재 여부 확인
                    boolean existsInRedis = isSessionExistsInRedis(sessionId);
                    log.debug("[REDIS-SESSION-REGISTRY] 세션 정보 확인 - sessionId: {}, expired: {}, existsInRedis: {}", 
                             sessionId, sessionInfo.isExpired(), existsInRedis);
                    
                    if (includeExpiredSessions || existsInRedis) {
                        sessions.add(sessionInfo);
                        log.debug("[REDIS-SESSION-REGISTRY] 세션 추가됨 - sessionId: {} (메모리 기준)", sessionId);
                    }
                }
            }
        } else {
            // 2️⃣ 인메모리 캐시에 없으면 Redis 직접 스캔 (컨테이너 재기동 대응)
            log.debug("[REDIS-SESSION-REGISTRY] 인메모리 캐시 없음 - Redis 직접 스캔 시작");
            
            String pattern = getRedisSessionPrefix() + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                log.debug("[REDIS-SESSION-REGISTRY] Redis 스캔 결과: {}개 세션 키 발견", keys.size());
                
                for (String key : keys) {
                    try {
                        // Redis에서 세션 데이터 조회
                        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(key);
                        
                        if (sessionData != null && !sessionData.isEmpty()) {
                            // SPRING_SECURITY_CONTEXT에서 Principal 추출
                            Object securityContextObj = sessionData.get("sessionAttr:SPRING_SECURITY_CONTEXT");
                            
                            if (securityContextObj != null) {
                                byte[] contextBytes = (byte[]) securityContextObj;
                                SecurityContext context = (SecurityContext) SerializationUtils.deserialize(contextBytes);
                                
                                if (context != null && context.getAuthentication() != null) {
                                    Object sessionPrincipal = context.getAuthentication().getPrincipal();
                                    
                                    // Principal이 일치하는지 확인
                                    if (principalsMatch(principal, sessionPrincipal)) {
                                        // 세션 ID 추출 (키에서 prefix 제거)
                                        String sessionId = key.replace(getRedisSessionPrefix(), "");
                                        
                                        // 마지막 요청 시간 추출
                                        Object lastAccessedTimeObj = sessionData.get("lastAccessedTime");
                                        long lastAccessedTime = lastAccessedTimeObj != null 
                                            ? Long.parseLong(lastAccessedTimeObj.toString()) 
                                            : System.currentTimeMillis();
                                        
                                        // SessionInformation 생성
                                        SessionInformation sessionInfo = new SessionInformation(
                                            sessionPrincipal,
                                            sessionId,
                                            new Date(lastAccessedTime)
                                        );
                                        
                                        sessions.add(sessionInfo);
                                        
                                        // 인메모리 캐시에도 추가 (다음번 조회 최적화)
                                        sessionIdToSessionInfo.put(sessionId, sessionInfo);
                                        principalToSessionIds.computeIfAbsent(principal, k -> new CopyOnWriteArraySet<>()).add(sessionId);
                                        
                                        log.debug("[REDIS-SESSION-REGISTRY] Redis에서 세션 발견 및 캐시 추가 - sessionId: {}", sessionId);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("[REDIS-SESSION-REGISTRY] Redis 세션 조회 중 오류: {}", e.getMessage());
                    }
                }
            } else {
                log.debug("[REDIS-SESSION-REGISTRY] Redis에서 세션 키를 찾을 수 없음");
            }
        }
        
        log.debug("[REDIS-SESSION-REGISTRY] 조회된 세션 수: {} (principal: {})", sessions.size(), principal);
        return sessions;
    }

    @Override
    public SessionInformation getSessionInformation(String sessionId) {
        log.debug("[REDIS-SESSION-REGISTRY] getSessionInformation 호출 - sessionId: {}", sessionId);
        
        SessionInformation sessionInfo = sessionIdToSessionInfo.get(sessionId);
        
        // 세션 정보 반환 (메모리 기반)
        
        return sessionInfo;
    }

    @Override
    public void refreshLastRequest(String sessionId) {
        SessionInformation sessionInfo = sessionIdToSessionInfo.get(sessionId);
        if (sessionInfo != null) {
            sessionInfo.refreshLastRequest();
            log.debug("[REDIS-SESSION-REGISTRY] 세션 갱신 - sessionId: {}", sessionId);
        }
    }

    @Override
    public void registerNewSession(String sessionId, Object principal) {
        log.info("[REDIS-SESSION-REGISTRY] 새 세션 등록 - sessionId: {}, principal: {}", sessionId, principal);
        
        // 세션 정보 생성
        SessionInformation sessionInfo = new SessionInformation(principal, sessionId, new Date());
        
        // 메모리 캐시 업데이트
        sessionIdToSessionInfo.put(sessionId, sessionInfo);
        
        principalToSessionIds.computeIfAbsent(principal, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        
        // Redis에 principal -> sessionIds 매핑 저장
        savePrincipalSessionMapping(principal, sessionId);
        
        log.info("[REDIS-SESSION-REGISTRY] 세션 등록 완료 - sessionId: {}", sessionId);
    }

    @Override
    public void removeSessionInformation(String sessionId) {
        log.info("[REDIS-SESSION-REGISTRY] 세션 제거 - sessionId: {}", sessionId);
        
        SessionInformation sessionInfo = sessionIdToSessionInfo.remove(sessionId);
        if (sessionInfo != null) {
            Object principal = sessionInfo.getPrincipal();
            
            Set<String> sessionIds = principalToSessionIds.get(principal);
            if (sessionIds != null) {
                sessionIds.remove(sessionId);
                if (sessionIds.isEmpty()) {
                    principalToSessionIds.remove(principal);
                    removePrincipalSessionMapping(principal);
                } else {
                    savePrincipalSessionMapping(principal, sessionIds);
                }
            }
        }
        
        log.info("[REDIS-SESSION-REGISTRY] 세션 제거 완료 - sessionId: {}", sessionId);
    }

    /**
     * Redis에서 세션 존재 여부 확인
     */
    private boolean isSessionExistsInRedis(String sessionId) {
        try {
            String redisKey = getRedisSessionPrefix() + sessionId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.warn("[REDIS-SESSION-REGISTRY] Redis 세션 확인 실패 - sessionId: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 메모리에서 세션 정보 제거
     */
    @SuppressWarnings("unused") // 향후 세션 정리 로직에서 사용 예정
    private void removeSessionFromMemory(String sessionId) {
        SessionInformation sessionInfo = sessionIdToSessionInfo.remove(sessionId);
        if (sessionInfo != null) {
            Object principal = sessionInfo.getPrincipal();
            Set<String> sessionIds = principalToSessionIds.get(principal);
            if (sessionIds != null) {
                sessionIds.remove(sessionId);
                if (sessionIds.isEmpty()) {
                    principalToSessionIds.remove(principal);
                }
            }
        }
        log.debug("[REDIS-SESSION-REGISTRY] 메모리에서 세션 제거 - sessionId: {}", sessionId);
    }

    /**
     * Redis에 principal -> sessionIds 매핑 저장
     */
    private void savePrincipalSessionMapping(Object principal, String sessionId) {
        try {
            String redisKey = PRINCIPAL_SESSION_KEY + principal.toString();
            Set<String> sessionIds = principalToSessionIds.get(principal);
            if (sessionIds != null && !sessionIds.isEmpty()) {
                redisTemplate.opsForSet().add(redisKey, (Object[]) sessionIds.toArray(new String[0]));
            }
        } catch (Exception e) {
            log.warn("[REDIS-SESSION-REGISTRY] Principal 매핑 저장 실패", e);
        }
    }

    /**
     * Redis에 principal -> sessionIds 매핑 저장 (Set 전체)
     */
    private void savePrincipalSessionMapping(Object principal, Set<String> sessionIds) {
        try {
            String redisKey = PRINCIPAL_SESSION_KEY + principal.toString();
            redisTemplate.delete(redisKey);
            if (!sessionIds.isEmpty()) {
                redisTemplate.opsForSet().add(redisKey, (Object[]) sessionIds.toArray(new String[0]));
            }
        } catch (Exception e) {
            log.warn("[REDIS-SESSION-REGISTRY] Principal 매핑 저장 실패", e);
        }
    }

    /**
     * Redis에서 principal 매핑 제거
     */
    private void removePrincipalSessionMapping(Object principal) {
        try {
            String redisKey = PRINCIPAL_SESSION_KEY + principal.toString();
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("[REDIS-SESSION-REGISTRY] Principal 매핑 제거 실패", e);
        }
    }

    /**
     * 만료된 세션 강제 만료 처리
     */
    public void expireSession(String sessionId) {
        SessionInformation sessionInfo = sessionIdToSessionInfo.get(sessionId);
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            sessionInfo.expireNow();
            log.info("[REDIS-SESSION-REGISTRY] 세션 강제 만료 - sessionId: {}", sessionId);
        }
    }
}
