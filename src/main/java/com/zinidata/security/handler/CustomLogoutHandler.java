package com.zinidata.security.handler;

// AuthService import 제거
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.service.AuditLogService;
import com.zinidata.audit.vo.AuditLogVO;

import java.time.LocalDateTime;

/**
 * 로그아웃 처리 핸들러
 * 
 * <p>Spring Security의 LogoutHandler를 구현하여 로그아웃 처리를 담당합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final AuditLogService auditLogService;
    private final SessionRegistry sessionRegistry;
    
    @Value("${app.code:NBZM}")
    private String appCode;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                      Authentication authentication) {
        
        String username = authentication != null ? authentication.getName() : "anonymous";
        HttpSession session = request.getSession(false);
        String sessionId = session != null ? session.getId() : null;
        
        log.info("=== CustomLogoutHandler 실행됨 === username: {}, sessionId: {}", username, sessionId);
        System.out.println("=== CustomLogoutHandler 콘솔 출력 === username: " + username);
        
        try {
            // 로그아웃 감사로그 저장
            saveLogoutAuditLog(request, username, sessionId);
            log.info("로그아웃 감사로그 저장 완료 - username: {}", username);
            
        } catch (Exception e) {
            log.error("로그아웃 감사로그 저장 실패 - username: {}", username, e);
        }
    }
    
    /**
     * 로그아웃 감사로그 저장
     */
    private void saveLogoutAuditLog(HttpServletRequest request, String username, String sessionId) {
        
        Long startTime = System.currentTimeMillis();
        
        try {
            // 세션에서 memNo 추출 (AuditLogService와 동일한 방식)
            Long memNo = getCurrentUserMemNo(request);
            
            AuditLogVO auditLog = new AuditLogVO();
            
            // 필수 필드 설정
            auditLog.setMemNo(memNo);
            auditLog.setPrjType(appCode);
            auditLog.setClientIp(getClientIpAddress(request));
            
            // 중복 로그인으로 인한 세션 만료인지 미리 확인
            boolean isConcurrentLogin = isConcurrentLoginLogout(sessionId);
            
            // request_uri 설정 (중복 로그인의 경우 통일성을 위해 로그아웃 API로 설정)
            if (isConcurrentLogin) {
                auditLog.setRequestUri("/api/auth/logout"); // 시스템 로그아웃도 일관성을 위해 로그아웃 URI 사용
            } else {
                auditLog.setRequestUri(request.getRequestURI());
            }
            
            auditLog.setHttpMethod(request.getMethod());
            auditLog.setParameters("{}"); // 로그아웃 시 사용자 파라미터 없음
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(sessionId);
            auditLog.setAccessTime(LocalDateTime.now());
            auditLog.setProcessingTime(System.currentTimeMillis() - startTime);
            auditLog.setReferrer(request.getHeader("Referer"));
            
            // 로그아웃 유형별 action_type과 target_resource 설정
            String logoutTypeParam = request.getParameter("logoutType");
            
            // JSON Body에서 logoutType 추출
            if (logoutTypeParam == null) {
                logoutTypeParam = extractLogoutTypeFromRequestBody(request);
            }
            
            // 디버깅 로그 추가
            log.info("=== CustomLogoutHandler 디버깅 ===");
            log.info("logoutTypeParam: {}", logoutTypeParam);
            log.info("모든 파라미터: {}", request.getParameterMap());
            log.info("Request URI: {}", request.getRequestURI());
            log.info("Request Method: {}", request.getMethod());
            log.info("Content-Type: {}", request.getContentType());
            
            log.info("isConcurrentLogin: {}", isConcurrentLogin);
            
            if (isConcurrentLogin) {
                // 중복 로그인으로 인한 강제 로그아웃
                auditLog.setActionType(AuditActionType.SYSTEM.name());
                auditLog.setTargetResource("concurrent-session-logout");
                log.info("로그아웃 유형: 중복 로그인");
            } /* DEPRECATED: session-expired 로직 - SessionExpirationFilter에서 처리됨
            else if ("session-expired".equals(logoutTypeParam)) {
                // 세션 만료에 의한 로그아웃 (DEPRECATED: SessionExpirationFilter에서 처리함)
                auditLog.setActionType(AuditActionType.SYSTEM.name());
                auditLog.setTargetResource("session-timeout");
                log.info("로그아웃 유형: 세션 만료 (DEPRECATED - 실제로는 SessionExpirationFilter에서 처리)");
                // TODO: 이 로직은 현재 사용되지 않음. JavaScript 세션 모니터링 비활성화로 호출되지 않음
                //       실제 세션 타임아웃은 SessionExpirationFilter에서 처리됨
            } */ else {
                // 사용자 직접 로그아웃 (logoutType="user" 또는 파라미터 없음)
                auditLog.setActionType(AuditActionType.API_CALL.name());
                auditLog.setTargetResource("api:/auth/logout");
                log.info("로그아웃 유형: 사용자 직접 (logoutType: {})", logoutTypeParam);
            }
            
            auditLog.setResultStatus("SUCCESS");
            
            auditLogService.saveAuditLogAsync(auditLog);
            
        } catch (Exception e) {
            log.error("감사로그 저장 중 오류 발생", e);
        }
    }
    

    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    /**
     * 현재 로그인한 사용자의 회원번호 조회 (AuditLogService와 동일한 로직)
     */
    private Long getCurrentUserMemNo(HttpServletRequest request) {
        try {
            // Spring Security Authentication에서 사용자 정보 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                // HTTP 세션에서 memNo 추출
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Long memNo = (Long) session.getAttribute("memNo");
                    if (memNo != null) {
                        log.debug("세션에서 회원번호 추출: {}", memNo);
                        return memNo;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("사용자 회원번호 조회 실패", e);
        }
        
        return null;
    }
    
    /**
     * 중복 로그인으로 인한 세션 만료인지 확인
     */
    private boolean isConcurrentLoginLogout(String sessionId) {
        try {
            if (sessionId == null) {
                return false;
            }
            
            SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);
            return sessionInfo != null && sessionInfo.isExpired();
            
        } catch (Exception e) {
            log.warn("세션 만료 상태 확인 실패 - sessionId: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * JSON 요청 본문에서 logoutType 추출
     */
    private String extractLogoutTypeFromRequestBody(HttpServletRequest request) {
        try {
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                String body = request.getReader().lines().collect(java.util.stream.Collectors.joining());
                if (body.contains("session-expired")) {
                    return "session-expired";
                } else if (body.contains("user")) {
                    return "user";
                }
            }
        } catch (Exception e) {
            log.debug("JSON body 파싱 실패 (무시 가능): {}", e.getMessage());
        }
        return null;
    }
} 