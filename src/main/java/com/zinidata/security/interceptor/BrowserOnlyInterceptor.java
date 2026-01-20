package com.zinidata.security.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import org.springframework.lang.NonNull;

/**
 * 브라우저 전용 접근 제어 인터셉터
 * 
 * <p>비정상적인 자동화 도구(curl, Postman 등)를 통한 API 접근을 차단합니다.</p>
 * <p>보고서 생성 등 핵심 비즈니스 API에 적용되어 데이터 스크래핑을 방지합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Component
@Slf4j
public class BrowserOnlyInterceptor implements HandlerInterceptor {

    /**
     * 브라우저 검증 활성화 여부
     * - develop: false (개발 편의성)
     * - staging: true (운영 환경 테스트)  
     * - production: true (실제 서비스)
     */
    @Value("${security.browser-check.enabled:false}")
    private boolean browserCheckEnabled;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                            @NonNull HttpServletResponse response, 
                            @NonNull Object handler) throws Exception {
        
        // 브라우저 검증이 비활성화된 경우 모든 요청 허용
        if (!browserCheckEnabled) {
            log.debug("[BROWSER_CHECK] 브라우저 검증 비활성화 - 모든 요청 허용");
            return true;
        }
        
        String requestUri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        log.info("[BROWSER_CHECK] API 접근 시도 - URI: {}, IP: {}, UserAgent: {}", 
                requestUri, clientIp, userAgent);
        
        // 1. User-Agent 검증
        if (!isValidBrowser(userAgent)) {
            log.warn("[BROWSER_CHECK] 브라우저가 아닌 접근 차단 - URI: {}, IP: {}, UserAgent: {}", 
                    requestUri, clientIp, userAgent);
            
            sendBrowserOnlyError(response);
            return false;
        }
        
        // 2. 세션 기반 추가 검증 (선택적)
        // if (!validateSession(request)) {
        //     log.warn("[BROWSER_CHECK] 세션 검증 실패 - URI: {}, IP: {}", requestUri, clientIp);
            
        //     sendSessionError(response);
        //     return false;
        // }
        
        log.info("[BROWSER_CHECK] 브라우저 검증 통과 - URI: {}", requestUri);
        return true;
    }
    
    /**
     * 유효한 브라우저인지 검증
     */
    private boolean isValidBrowser(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return false;
        }
        
        String lowerUserAgent = userAgent.toLowerCase();
        
        // 1. 자동화 도구 및 의심스러운 패턴 차단
        String[] suspiciousPatterns = {
            "curl", "wget", "postman", "insomnia", "httpie",
            "python-urllib", "python-requests", "java/",
            "node.js", "php/", "perl/", "ruby/", "go-http-client",
            "bot", "crawler", "spider", "scraper", "automation"
        };
        
        for (String pattern : suspiciousPatterns) {
            if (lowerUserAgent.contains(pattern)) {
                return false;
            }
        }
        
        // 2. 정상 브라우저 패턴 확인
        boolean hasMozilla = lowerUserAgent.contains("mozilla");
        boolean hasValidBrowser = lowerUserAgent.contains("chrome") ||
                                 lowerUserAgent.contains("firefox") ||
                                 lowerUserAgent.contains("safari") ||
                                 lowerUserAgent.contains("edge") ||
                                 lowerUserAgent.contains("opera");
        
        return hasMozilla && hasValidBrowser;
    }
    
    /**
     * 세션 검증 (로그인된 사용자인지 확인)
     */
    private boolean validateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        // 세션에 로그인 정보가 있는지 확인
        Object memNo = session.getAttribute("memNo");
        Object loginId = session.getAttribute("loginId");
        
        return memNo != null && loginId != null;
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
     * 브라우저 전용 오류 응답
     */
    private void sendBrowserOnlyError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        
        String errorResponse = """
            {
                "success": false,
                "code": "BROWSER_ONLY",
                "message": "이 서비스는 웹 브라우저에서만 이용 가능합니다.",
                "detail": "자동화 도구나 API 클라이언트를 통한 접근은 제한됩니다."
            }
            """;
        
        response.getWriter().write(errorResponse);
    }
    
    /**
     * 세션 오류 응답
     */
    private void sendSessionError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        
        String errorResponse = """
            {
                "success": false,
                "code": "SESSION_REQUIRED",
                "message": "로그인이 필요합니다.",
                "detail": "인증된 사용자만 이용 가능한 서비스입니다."
            }
            """;
        
        response.getWriter().write(errorResponse);
    }
}
