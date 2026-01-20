package com.zinidata.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.zinidata.audit.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 세션 설정
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 1800, // 30분 (1800초) - 어노테이션이 우선함
    redisNamespace = "${spring.application.name}:session" // 동적 네임스페이스
)
@RequiredArgsConstructor
public class SessionConfig {

    private final AuditLogService auditLogService;
    
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Redis 메시지 리스너 컨테이너 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 키 만료 이벤트 리스너 등록
        RedisKeyExpirationListener listener = new RedisKeyExpirationListener(auditLogService, applicationName);
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(listener, "onMessage");
        container.addMessageListener(listenerAdapter, 
            org.springframework.data.redis.listener.PatternTopic.of("__keyevent@*__:expired"));
        
        log.info("Redis 키 만료 이벤트 리스너 등록 완료 - namespace: {}:session", applicationName);
        return container;
    }

    /**
     * Redis 키 만료 이벤트 리스너
     * 세션 키가 만료될 때 감사 로그를 생성합니다.
     */
    @Slf4j
    @RequiredArgsConstructor
    public static class RedisKeyExpirationListener {
        
        @SuppressWarnings("unused") // Redis 이벤트 처리용 의존성 (향후 확장 예정)
        private final AuditLogService auditLogService;
        private final String applicationName;
        
        /**
         * Redis 키 만료 이벤트 처리
         * 
         * @param message 만료된 키 정보
         */
        public void onMessage(String message) {
            try {
                log.debug("Redis 키 만료 이벤트 수신: {}", message);
                
                // 동적 세션 키 패턴 생성
                String sessionKeyPrefix = applicationName + ":session:sessions:";
                
                // 우리 애플리케이션의 Session 키인지 확인
                if (message != null && message.startsWith(sessionKeyPrefix)) {
                    String sessionId = message.substring(sessionKeyPrefix.length());
                    log.info("세션 만료 감지: sessionId={}, prefix={}", sessionId, sessionKeyPrefix);
                    
                    // 세션 만료로 인한 로그아웃 감사 로그 생성
                    createSessionExpirationAuditLog(sessionId);
                }
                
            } catch (Exception e) {
                log.error("Redis 키 만료 이벤트 처리 중 오류: {}", e.getMessage(), e);
            }
        }
        
        /**
         * Redis 레벨 세션 만료 이벤트 로깅 (모니터링용)
         * 
         * @param sessionId 만료된 세션 ID
         */
        private void createSessionExpirationAuditLog(String sessionId) {
            // ✅ 표준: Redis 인프라 레벨 모니터링 로그만 출력 (감사로그는 SessionExpirationFilter에서 처리)
            log.info("[REDIS-MONITOR] 세션 키 만료 감지: sessionId={}", sessionId);
            log.debug("[REDIS-MONITOR] 감사로그는 SessionExpirationFilter에서 처리됨");
        }
    }
} 