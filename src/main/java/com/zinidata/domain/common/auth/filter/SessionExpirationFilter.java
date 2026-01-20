package com.zinidata.domain.common.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.enums.AuditResultStatus;
import com.zinidata.audit.service.AuditLogService;
import com.zinidata.audit.vo.AuditLogVO;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 만료 감지 필터
 * 세션이 만료되었을 때 감사 로그를 생성합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpirationFilter extends OncePerRequestFilter {

    // 세션 추적을 위한 맵 (세션 ID -> 사용자 정보)
    private final ConcurrentHashMap<String, SessionInfo> sessionTracker = new ConcurrentHashMap<>();
    
    // 감사로그 서비스 의존성 주입 (DevTools 재시작 충돌 방지를 위한 지연 로딩)
    @Lazy
    private final org.springframework.beans.factory.ObjectProvider<AuditLogService> auditLogServiceProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 현재 세션 확인
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // 세션에서 사용자 정보 조회
                Long memNo = (Long) session.getAttribute("memNo");
                String loginId = (String) session.getAttribute("loginId");
                String sessionId = session.getId();
                
                if (memNo != null && loginId != null) {
                    // 유효한 세션 - 추적 정보 업데이트
                    updateSessionTracker(sessionId, memNo, loginId, request);
                    log.debug("[AUTH-V1] 세션 추적 정보 업데이트: sessionId={}, memNo={}", sessionId, memNo);
                } else {
                    // 세션은 있지만 사용자 정보가 없음 - 이전에 추적하던 세션인지 확인
                    checkSessionExpiration(sessionId);
                }
            } else {
                // 세션이 없는 경우 - 이전에 추적하던 세션들이 만료되었는지 확인
                checkAllExpiredSessions();
            }
            
        } catch (Exception e) {
            log.error("[AUTH-V1] 세션 만료 필터 처리 중 오류", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 세션 추적 정보 업데이트
     */
    private void updateSessionTracker(String sessionId, Long memNo, String loginId, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        SessionInfo sessionInfo = new SessionInfo(memNo, loginId, System.currentTimeMillis(), clientIp, userAgent);
        sessionTracker.put(sessionId, sessionInfo);
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 특정 세션의 만료 확인
     */
    private void checkSessionExpiration(String sessionId) {
        SessionInfo sessionInfo = sessionTracker.get(sessionId);
        if (sessionInfo != null) {
            log.info("[AUTH-V1] 세션 만료 감지: sessionId={}, memNo={}, loginId={}", 
                    sessionId, sessionInfo.getMemNo(), sessionInfo.getLoginId());
            
            // 세션 만료 감사 로그 생성
            createSessionExpirationAuditLog(sessionInfo, sessionId);
            
            // 추적 정보에서 제거
            sessionTracker.remove(sessionId);
        }
    }
    
    /**
     * 모든 만료된 세션 확인
     */
    private void checkAllExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long sessionTimeout = 1800 * 1000; // 30분 (1800초) (application.yml 설정과 동일)
        
        sessionTracker.entrySet().removeIf(entry -> {
            String sessionId = entry.getKey();
            SessionInfo sessionInfo = entry.getValue();
            
            // 세션 타임아웃 확인
            if (currentTime - sessionInfo.getLastAccessTime() > sessionTimeout) {
                log.info("[AUTH-V1] 세션 타임아웃 감지: sessionId={}, memNo={}, loginId={}", 
                        sessionId, sessionInfo.getMemNo(), sessionInfo.getLoginId());
                
                // 세션 만료 감사 로그 생성
                createSessionExpirationAuditLog(sessionInfo, sessionId);
                return true; // 맵에서 제거
            }
            return false;
        });
    }
    
    /**
     * 세션 만료 감사 로그 생성 (표준 구현)
     */
    private void createSessionExpirationAuditLog(SessionInfo sessionInfo, String sessionId) {
        log.info("[AUTH-V1] 세션 타임아웃 감지: memNo={}, loginId={}, sessionId={}", 
                sessionInfo.getMemNo(), sessionInfo.getLoginId(), sessionId);
        
        try {
            // ObjectProvider를 통해 실제 사용 시점에 Bean 획득 (지연 로딩)
            AuditLogService auditLogService = auditLogServiceProvider.getObject();
            AuditLogVO auditLog = auditLogService.createAuditLog(
                null, // HttpServletRequest - 세션 타임아웃은 시스템 이벤트이므로 null
                null, // 메서드 인자 없음
                AuditActionType.SYSTEM,
                "session-timeout", // 세션 타임아웃으로 target_resource 설정
                "자동 세션 만료로 인한 로그아웃",
                AuditResultStatus.SUCCESS,
                null, // 에러 메시지 없음
                0L, // 처리 시간 0
                new String[]{} // 민감정보 필드 없음
            );
            
            // ✅ 표준: SessionInfo에서 사용자 정보 수동 설정
            auditLog.setMemNo(sessionInfo.getMemNo());
            auditLog.setSessionId(sessionId);
            auditLog.setClientIp(sessionInfo.getClientIp());
            auditLog.setUserAgent(sessionInfo.getUserAgent());
            auditLog.setRequestUri("/api/auth/logout"); // 일관성을 위해 로그아웃 URI 사용
            auditLog.setHttpMethod("POST");
            auditLog.setParameters("{}");
            
            // ✅ 표준: 비동기로 감사 로그 저장
            auditLogService.saveAuditLogAsync(auditLog);
            
            log.info("[AUTH-V1] 세션 타임아웃 감사로그 저장 완료 - memNo: {}, sessionId: {}", 
                    sessionInfo.getMemNo(), sessionId);
            
        } catch (Exception e) {
            log.error("[AUTH-V1] 세션 타임아웃 감사로그 저장 실패 - memNo: {}, sessionId: {}", 
                    sessionInfo.getMemNo(), sessionId, e);
        }
    }
    
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        // 정적 리소스나 공개 API는 필터링하지 않음
        String path = request.getRequestURI();
        return path.startsWith("/static/") || 
               path.startsWith("/assets/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") || 
               path.startsWith("/favicon.ico") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/");
    }
    
    /**
     * 세션 정보를 담는 내부 클래스
     */
    private static class SessionInfo {
        private final Long memNo;
        private final String loginId;
        private final String clientIp;     // 🔍 IP 기반 접근 패턴 분석
        private final String userAgent;    // 🤖 Bot/스크래퍼 탐지
        private final long lastAccessTime;
        
        public SessionInfo(Long memNo, String loginId, long lastAccessTime, String clientIp, String userAgent) {
            this.memNo = memNo;
            this.loginId = loginId;
            this.lastAccessTime = lastAccessTime;
            this.clientIp = clientIp;
            this.userAgent = userAgent;
        }
        
        public Long getMemNo() { return memNo; }
        public String getLoginId() { return loginId; }
        public long getLastAccessTime() { return lastAccessTime; }
        public String getClientIp() { return clientIp; }
        public String getUserAgent() { return userAgent; }
    }
} 