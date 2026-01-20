package com.zinidata.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zinidata.security.properties.SecurityProperties;

import java.io.IOException;
import java.time.Duration;

import org.springframework.lang.NonNull;

/**
 * Redis 기반 Rate Limiting 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final SecurityProperties securityProperties;
    
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String RATE_LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String RATE_LIMIT_RESET_HEADER = "X-RateLimit-Reset";
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Rate limiting이 비활성화된 경우 통과
        if (!securityProperties.getRateLimit().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 정적 리소스와 헬스체크는 제외
        String uri = request.getRequestURI();
        if (isExcludedPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientId = getClientId(request);
        String key = RATE_LIMIT_KEY_PREFIX + clientId;
        
        try {
            // 현재 요청 수 확인
            String currentCountStr = redisTemplate.opsForValue().get(key);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            int limit = securityProperties.getRateLimit().getRequestsPerMinute();
            int remaining = Math.max(0, limit - currentCount - 1);
            
            // 응답 헤더 설정
            response.setHeader(RATE_LIMIT_HEADER, String.valueOf(limit));
            response.setHeader(RATE_LIMIT_REMAINING_HEADER, String.valueOf(remaining));
            response.setHeader(RATE_LIMIT_RESET_HEADER, String.valueOf(System.currentTimeMillis() + 60000));
            
            // Rate limit 체크
            if (currentCount >= limit) {
                log.warn("Rate limit exceeded for client: {}, current count: {}, limit: {}", 
                        clientId, currentCount, limit);
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests\"}");
                return;
            }
            
            // 카운트 증가
            if (currentCount == 0) {
                // 새로운 키인 경우 TTL 설정
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            } else {
                // 기존 키인 경우 카운트만 증가
                redisTemplate.opsForValue().increment(key);
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Rate limiting error for client: {}", clientId, e);
            // 에러 발생 시 요청 통과
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * 클라이언트 ID 추출
     */
    private String getClientId(HttpServletRequest request) {
        // 인증된 사용자가 있는 경우 사용자 ID 사용
        String userPrincipal = request.getRemoteUser();
        if (userPrincipal != null) {
            return "user:" + userPrincipal;
        }
        
        // IP 주소 기반 제한
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Rate limiting 제외 경로 확인
     */
    private boolean isExcludedPath(String uri) {
        return uri.startsWith("/assets/") ||
               uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/favicon.ico") ||
               uri.startsWith("/actuator/health") ||
               uri.startsWith("/actuator/info") ||
               uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/logout");
    }
} 