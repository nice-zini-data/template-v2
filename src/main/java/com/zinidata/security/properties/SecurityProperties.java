package com.zinidata.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 보안 설정 프로퍼티
 */
@Data
@Component
@ConfigurationProperties(prefix = "custom.security")
public class SecurityProperties {
    
    /**
     * 세션 관련 설정
     */
    private Session session = new Session();
    
    /**
     * Rate Limit 설정
     */
    private RateLimit rateLimit = new RateLimit();
    
    /**
     * CORS 설정
     */
    private Cors cors = new Cors();
    
    /**
     * CSRF 설정
     */
    private Csrf csrf = new Csrf();
    
    @Data
    public static class Session {
        private int maxSessions = 1;
        private boolean preventLoginIfMaximumExceeded = false;
        private boolean sessionRegistryEnabled = true;
    }
    
    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 60;
        private int burstCapacity = 100;
    }
    
    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000,http://localhost:8090";
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        private int maxAge = 3600;
    }
    
    @Data
    public static class Csrf {
        private boolean enabled = true;
        private boolean cookieHttpOnly = true;
        private boolean cookieSecure = false;
    }
    
    public boolean isCsrfEnabled() {
        return csrf.isEnabled();
    }
    
    public int getMaxSessions() {
        return session.getMaxSessions();
    }
    
    public boolean isPreventLoginIfMaximumExceeded() {
        return session.isPreventLoginIfMaximumExceeded();
    }
    
    public boolean isSessionRegistryEnabled() {
        return session.isSessionRegistryEnabled();
    }
} 