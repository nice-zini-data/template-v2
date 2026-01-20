package com.zinidata.security.config;

import com.zinidata.audit.service.AuditLogService;
import com.zinidata.audit.vo.AuditLogVO;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.enums.AuditResultStatus;
import com.zinidata.security.properties.SecurityProperties;
import com.zinidata.security.session.RedisSessionRegistry;
import com.zinidata.security.dto.v1.CustomUserDetailsV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;

import java.util.Arrays;

/**
 * Spring Security 세션 관련 설정
 * 순환 의존성 문제 해결을 위해 SessionRegistry 빈을 별도로 분리
 */
@Configuration
@RequiredArgsConstructor
public class SecuritySessionConfig {

    private final SecurityProperties securityProperties;
    private final org.springframework.beans.factory.ObjectProvider<AuditLogService> auditLogServiceProvider;

    /**
     * Redis 기반 세션 레지스트리 - 중복 로그인 차단을 위한 핵심 컴포넌트
     */
    @Bean
    public SessionRegistry sessionRegistry(RedisSessionRegistry redisSessionRegistry) {
        System.out.println("🔧 [SESSION-REGISTRY] Redis 기반 SessionRegistry Bean 생성 완료");
        return redisSessionRegistry;
    }

    /**
     * 세션 인증 전략 - 중복 로그인 차단 설정 포함
     */
    @Bean
    public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
        // 중복 세션 제어 전략에 maxSessions 설정
        ConcurrentSessionControlAuthenticationStrategy concurrentStrategy = 
            new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry) {
                @Override
                public void onAuthentication(org.springframework.security.core.Authentication authentication, 
                        jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) 
                        throws org.springframework.security.web.authentication.session.SessionAuthenticationException {
                    System.out.println("🚨 [CONCURRENT-STRATEGY] onAuthentication 실행됨!");
                    System.out.println("🚨 [CONCURRENT-STRATEGY] Principal: " + authentication.getPrincipal());
                    
                    // 🔍 기존 세션 정보 확인
                    Object principal = authentication.getPrincipal();
                    var sessions = sessionRegistry.getAllSessions(principal, false);
                    System.out.println("🔍 [CONCURRENT-STRATEGY] 기존 세션 수: " + sessions.size());
                    for (var session : sessions) {
                        System.out.println("🔍 [CONCURRENT-STRATEGY] 기존 세션: " + session.getSessionId() + 
                                         ", 만료됨: " + session.isExpired());
                    }
                    
                    // 🔧 정석적인 방법: 중복 로그인 감지 시 감사로그 먼저 생성 후 Spring Security 기본 동작 실행
                    if (sessions.size() >= getMaximumSessionsForThisUser(authentication)) {
                        System.out.println("🚨 [CONCURRENT-STRATEGY] 중복 로그인 감지! 기존 세션 강제 만료 처리");
                        System.out.println("🚨 [CONCURRENT-STRATEGY] maxSessions: " + getMaximumSessionsForThisUser(authentication));
                        System.out.println("🚨 [CONCURRENT-STRATEGY] 현재 세션 수: " + sessions.size());
                        
                        for (var session : sessions) {
                            System.out.println("🔍 [CONCURRENT-STRATEGY] 처리 전 세션: " + session.getSessionId() + ", expired: " + session.isExpired());
                            createConcurrentLoginAuditLog(session, authentication, request);
                            System.out.println("🚨 [CONCURRENT-STRATEGY] 중복 로그인 감사로그 생성 완료: " + session.getSessionId());
                        }
                        
                        super.onAuthentication(authentication, request, response);
                        System.out.println("🚨 [CONCURRENT-STRATEGY] Spring Security 기본 중복 세션 처리 완료");
                    } else {
                        System.out.println("🔍 [CONCURRENT-STRATEGY] 중복 로그인 아님 - 기존 세션 수: " + sessions.size() + ", 허용 수: " + getMaximumSessionsForThisUser(authentication));
                    }
                    
                    var sessionsAfter = sessionRegistry.getAllSessions(principal, false);
                    System.out.println("🔍 [CONCURRENT-STRATEGY] 인증 후 세션 수: " + sessionsAfter.size());
                    for (var session : sessionsAfter) {
                        System.out.println("🔍 [CONCURRENT-STRATEGY] 인증 후 세션: " + session.getSessionId() + 
                                         ", 만료됨: " + session.isExpired());
                    }
                }
            };
        concurrentStrategy.setMaximumSessions(securityProperties.getMaxSessions());
        concurrentStrategy.setExceptionIfMaximumExceeded(securityProperties.isPreventLoginIfMaximumExceeded());
        System.out.println("🔧 [CONCURRENT-STRATEGY] maxSessions: " + securityProperties.getMaxSessions());
        System.out.println("🔧 [CONCURRENT-STRATEGY] exceptionIfMaximumExceeded: " + securityProperties.isPreventLoginIfMaximumExceeded());
        return new CompositeSessionAuthenticationStrategy(Arrays.asList(
            concurrentStrategy,
            new SessionFixationProtectionStrategy(),
            new RegisterSessionAuthenticationStrategy(sessionRegistry)
        ));
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @EventListener
    public void handleSessionCreated(SessionCreatedEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("🔍 [SESSION-EVENT] 세션 생성 이벤트: " + sessionId);
    }

    @EventListener
    public void handleSessionDeleted(SessionDeletedEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("🔍 [SESSION-EVENT] 세션 삭제 이벤트: " + sessionId);
    }

    @EventListener  
    public void handleSessionExpired(SessionExpiredEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("🔍 [SESSION-EVENT] 세션 만료 이벤트: " + sessionId);
    }

    /**
     * 중복 로그인 감사로그 생성 (ObjectProvider로 실제 사용 시점에 Bean 획득)
     */
    private void createConcurrentLoginAuditLog(
            org.springframework.security.core.session.SessionInformation expiredSession,
            org.springframework.security.core.Authentication newAuthentication,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            CustomUserDetailsV1 userDetails = (CustomUserDetailsV1) newAuthentication.getPrincipal();
            AuditLogService auditLogService = auditLogServiceProvider.getObject();
            AuditLogVO auditLog = auditLogService.createAuditLog(
                request,
                null,
                AuditActionType.SYSTEM,
                "concurrent-session-logout",
                "중복 로그인으로 인한 기존 세션 강제 만료",
                AuditResultStatus.SUCCESS,
                null,
                0L,
                new String[]{}
            );
            auditLog.setMemNo(userDetails.getMemNo());
            auditLog.setSessionId(expiredSession.getSessionId());
            auditLog.setRequestUri("/auth/login");
            auditLog.setHttpMethod("POST");
            auditLog.setParameters("{}");
            auditLogService.saveAuditLogAsync(auditLog);
            System.out.println("🔧 [CONCURRENT-STRATEGY] 중복 로그인 감사로그 저장 완료 - sessionId: " + expiredSession.getSessionId());
        } catch (Exception e) {
            System.err.println("🚨 [CONCURRENT-STRATEGY] 중복 로그인 감사로그 저장 실패: " + e.getMessage());
        }
    }
} 