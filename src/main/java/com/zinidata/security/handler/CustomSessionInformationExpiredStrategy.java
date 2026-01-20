package com.zinidata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 세션 만료 처리 전략 (중복 로그인 등)
 * 
 * <p>중복 로그인으로 인한 세션 만료 시 JSON 응답으로 처리합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

    private final ObjectMapper objectMapper;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();
        
        String username = event.getSessionInformation().getPrincipal().toString();
        String sessionId = event.getSessionInformation().getSessionId();
        
        log.info("[SESSION-EXPIRED] 만료된 세션 접근 감지 - username: {}, sessionId: {} (감사로그는 ConcurrentSessionControlAuthenticationStrategy에서 이미 생성됨)", username, sessionId);
        
        // AJAX 요청인지 확인
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        
        if ("XMLHttpRequest".equals(requestedWith) || 
            (accept != null && accept.contains("application/json"))) {
            
            // AJAX 요청: JSON 응답
            handleAjaxRequest(response, username);
            
        } else {
            
            // 일반 요청: 메시지와 함께 리다이렉트
            handleNormalRequest(response, username);
        }
    }
    
    /**
     * AJAX 요청 처리 (JSON 응답)
     */
    private void handleAjaxRequest(HttpServletResponse response, String username) throws IOException {
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", false);
        responseData.put("code", "SESSION_EXPIRED");
        responseData.put("message", "다른 기기에서 로그인하여 현재 세션이 종료되었습니다. 다시 로그인해주세요.1");
        responseData.put("action", "REDIRECT");
        responseData.put("redirectUrl", "/auth/login");
        responseData.put("username", username);
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        objectMapper.writeValue(response.getWriter(), responseData);
    }
    
    /**
     * 일반 요청 처리 (alert + 리다이렉트)
     */
    private void handleNormalRequest(HttpServletResponse response, String username) throws IOException {
        
        // JavaScript alert + 리다이렉트 HTML 생성
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>세션 만료</title>
            </head>
            <body>
                <script>
                    alert('다른 기기에서 로그인하여 현재 세션이 종료되었습니다. 다시 로그인해주세요.2');
                    window.location.href = '/auth/login';
                </script>
            </body>
            </html>
            """;
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
    }
} 