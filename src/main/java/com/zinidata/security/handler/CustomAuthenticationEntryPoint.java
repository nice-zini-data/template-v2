package com.zinidata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 진입점 핸들러
 * 
 * 요청 타입에 따라 다르게 처리:
 * - API 요청 (/api/**): JSON 응답 반환
 * - 웹 페이지 요청: 로그인 페이지로 리다이렉트
 * 
 * TODO: 추후 claude-4-sonnet 에게 검토 요청 필요
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        
        log.warn("Authentication required for request: {}, reason: {}", 
                requestURI, authException.getMessage());
        
        // 세션 만료 여부 확인
        // TODO: 추후 claude-4-sonnet 에게 검토 요청 필요
        // 질문: 로그인하지 않은 사용자도 세션이 만료되면 강제로 /auth/login 여기로 가는거 같아서 아래와 같이 코드 변경했는데 잘 됐는지 검토 필요
        // WebSecurityConfig.java의 .sessionManagement(session -> session) 부분과 연동하여 수정함
        boolean isSessionExpired = request.getSession(false) != null && 
                                  request.getSession(false).getAttribute("SPRING_SECURITY_LAST_EXCEPTION") != null;
        
        // API 요청인지 확인 (/api/로 시작하거나 JSON을 요청하는 경우)
        boolean isApiRequest = requestURI.startsWith("/api/") || 
                              (acceptHeader != null && acceptHeader.contains("application/json"));
        
        if (isApiRequest) {
            // API 요청: JSON 응답 반환
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", false);
            responseData.put("message", isSessionExpired ? "세션이 만료되었습니다" : "인증이 필요합니다");
            responseData.put("error", isSessionExpired ? "Session expired" : "Authentication required");
            responseData.put("path", requestURI);
            responseData.put("timestamp", System.currentTimeMillis());
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            objectMapper.writeValue(response.getWriter(), responseData);
        } else {
            // 웹 페이지 요청: 로그인 페이지로 리다이렉트
            String redirectUrl = "/auth/login?redirect=" + requestURI;
            if (isSessionExpired) {
                redirectUrl += "&expired=true";
            }
            
            log.info("Redirecting to login page for web request: {} (expired: {})", requestURI, isSessionExpired);
            response.sendRedirect(redirectUrl);
        }
    }
} 