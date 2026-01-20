package com.zinidata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinidata.common.util.CookieUtil;
import com.zinidata.common.util.JwtTokenUtil;
import com.zinidata.domain.common.auth.mapper.AuthMapper;
import com.zinidata.domain.common.auth.vo.MemberVO;
import com.zinidata.security.dto.v1.CustomUserDetailsV1;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 성공 핸들러
 * 
 * <p>Spring Security 인증 성공 후 세션 데이터 저장 및 응답 처리를 담당합니다.</p>
 * <p>세션 고정 공격 방지는 Spring Security에서 자동 처리됩니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final AuthMapper authMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        CustomUserDetailsV1 userDetails = (CustomUserDetailsV1) authentication.getPrincipal();
        String loginId = userDetails.getUsername();
        
        log.info("[AUTH_SUCCESS] 인증 성공 처리 시작: loginId={}", loginId);
        
        // returnUrl 파라미터 확인
        String returnUrl = request.getParameter("returnUrl");
        log.info("[AUTH_SUCCESS] returnUrl 파라미터: {}", returnUrl);
        
        // 1. 세션 정보 저장 (Spring Security가 이미 세션 ID 재생성 완료)
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String currentTime = getCurrentTime();
        
        // 2. 세션에 사용자 정보 저장 (문서 요구사항 준수)
        session.setAttribute("memNo", userDetails.getMemNo());
        session.setAttribute("loginId", userDetails.getLoginId());
        session.setAttribute("email", userDetails.getEmailAddr());
        session.setAttribute("mobileNo", userDetails.getMobileNo());
        session.setAttribute("name", userDetails.getMemNm());
        session.setAttribute("memStat", userDetails.getMemStat());
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("loginTime", currentTime);
        session.setAttribute("clientIp", clientIp);
        
        // 3. 추가 보안 정보 저장
        session.setAttribute("userAgent", userAgent);
        session.setAttribute("loginType", "NORMAL");
        session.setAttribute("tempPwdYn", userDetails.getTempPwdYn() != null ? userDetails.getTempPwdYn() : "N");
        
        log.info("[AUTH_SUCCESS] 세션 정보 저장 완료: memNo={}, loginId={}, sessionId={}", 
                userDetails.getMemNo(), loginId, sessionId);
        
        // 4. DB 세션 정보 업데이트
        try {
            authMapper.updateMemberSession(userDetails.getMemNo(), sessionId, null, System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("[AUTH_SUCCESS] DB 세션 업데이트 실패: memNo={}", userDetails.getMemNo(), e);
        }
        
        // 5. JWT 토큰 발급 및 쿠키 설정 (SSO용)
        try {
            // MemberVO 객체 생성
            MemberVO member = new MemberVO();
            member.setMemNo(userDetails.getMemNo());
            member.setLoginId(userDetails.getLoginId());
            member.setMemNm(userDetails.getMemNm());
            member.setEmailAddr(userDetails.getEmailAddr());
            member.setMobileNo(userDetails.getMobileNo());
            member.setMemType(userDetails.getMemType());
            
            String jwtToken = jwtTokenUtil.generateNormalLoginToken(member);
            cookieUtil.setAuthTokenCookie(response, jwtToken);
            log.info("[AUTH_SUCCESS] JWT 토큰 발급 완료: memNo={}, loginId={}", userDetails.getMemNo(), userDetails.getLoginId());
        } catch (Exception e) {
            log.error("[AUTH_SUCCESS] JWT 토큰 발급 실패: memNo={}", userDetails.getMemNo(), e);
            // JWT 토큰 발급 실패해도 로그인은 성공으로 처리
        }
        
        // 6. returnUrl에 따른 리다이렉트 URL 결정
        String redirectUrl = null;
        if (returnUrl != null && !returnUrl.isEmpty()) {
            log.info("[AUTH_SUCCESS] returnUrl 파라미터 처리: {}", returnUrl);
            
            // returnUrl 보안 검증 (화이트리스트 방식)
            if (isValidReturnUrl(returnUrl)) {
                // openBetterBoss 파라미터 추가
                if (returnUrl.contains("?")) {
                    redirectUrl = returnUrl + "&openBetterBoss=true";
                } else {
                    redirectUrl = returnUrl + "?openBetterBoss=true";
                }
                log.info("[AUTH_SUCCESS] 리다이렉트 URL 설정: {}", redirectUrl);
            } else {
                log.warn("[AUTH_SUCCESS] 유효하지 않은 returnUrl, 기본 페이지로 리다이렉트: {}", returnUrl);
                redirectUrl = "/";
            }
        }
        
        // 7. 기본 성공 응답 생성 (프론트엔드 기대 구조에 맞춤)
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("memNo", userDetails.getMemNo());
        loginData.put("loginId", loginId);
        loginData.put("memNm", userDetails.getMemNm());
        loginData.put("memType", userDetails.getMemType());
        loginData.put("emailAddr", userDetails.getEmailAddr());
        loginData.put("mobileNo", userDetails.getMobileNo());
        loginData.put("memStat", userDetails.getMemStat());
        loginData.put("sessionId", sessionId);
        loginData.put("loginTimestamp", currentTime);
        loginData.put("tempPwdYn", userDetails.getTempPwdYn());
        
        // redirectUrl이 있으면 포함
        if (redirectUrl != null) {
            loginData.put("redirectUrl", redirectUrl);
            log.info("[AUTH_SUCCESS] 응답에 리다이렉트 URL 포함: {}", redirectUrl);
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "로그인 성공");
        responseData.put("data", loginData);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        objectMapper.writeValue(response.getWriter(), responseData);
        
        log.info("[AUTH_SUCCESS] 로그인 성공 처리 완료: loginId={}", loginId);
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 현재 시간 문자열 반환
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * returnUrl 유효성 검증 (보안 화이트리스트 방식)
     * @param url 검증할 URL
     * @return 유효한 URL 여부
     */
    private boolean isValidReturnUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // 외부 URL 차단 (Open Redirect 방지)
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) {
            log.warn("[AUTH_SUCCESS] 외부 URL 차단: {}", url);
            return false;
        }
        
        // 허용된 경로 패턴 (화이트리스트)
        String[] allowedPaths = {
            "/",                    // 홈
            "/explorer/",           // 상권 분석
            "/pricing/",            // 가격
            "/support/",            // 고객지원
            "/mypage/",             // 마이페이지
            "/event/",              // 이벤트
            "/company/",            // 회사소개
            "/terms/"               // 약관
        };
        
        for (String allowedPath : allowedPaths) {
            if (url.startsWith(allowedPath)) {
                return true;
            }
        }
        
        log.warn("[AUTH_SUCCESS] 허용되지 않은 경로: {}", url);
        return false;
    }
} 