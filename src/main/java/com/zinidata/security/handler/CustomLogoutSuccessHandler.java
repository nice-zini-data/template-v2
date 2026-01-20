package com.zinidata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그아웃 성공 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) throws IOException {
        
        String username = authentication != null ? authentication.getName() : "anonymous";
        log.info("Logout success for user: {}", username);
        
        // 성공 응답 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "로그아웃 성공");
        responseData.put("username", username);
        responseData.put("timestamp", System.currentTimeMillis());
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        objectMapper.writeValue(response.getWriter(), responseData);
    }
} 