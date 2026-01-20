package com.zinidata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 접근 거부 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        
        log.warn("Access denied for user: {}, path: {}, reason: {}", 
                username, request.getRequestURI(), accessDeniedException.getMessage());
        
        // 접근 거부 응답 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", false);
        responseData.put("message", "접근이 거부되었습니다");
        responseData.put("error", "Access denied");
        responseData.put("path", request.getRequestURI());
        responseData.put("username", username);
        responseData.put("timestamp", System.currentTimeMillis());
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        objectMapper.writeValue(response.getWriter(), responseData);
    }
} 