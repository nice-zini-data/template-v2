package com.zinidata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 실패 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException {
        
        log.warn("Authentication failed for user: {}, reason: {}", 
                request.getParameter("username"), exception.getMessage());
        
        // 실패 응답 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", false);
        responseData.put("message", "로그인 실패");
        responseData.put("error", exception.getMessage());
        responseData.put("timestamp", System.currentTimeMillis());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        objectMapper.writeValue(response.getWriter(), responseData);
    }
} 