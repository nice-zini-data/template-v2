package com.zinidata.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 쿠키 설정 유틸리티
 * 
 * <p>A 서버에서 B 서버로 JWT 토큰을 전달하기 위한 쿠키를 설정합니다.</p>
 * <p>도메인 간 공유를 위해 .nicebizmap.co.kr로 설정합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
public class CookieUtil {
    
    /**
     * JWT 인증 토큰 쿠키 이름
     */
    private static final String AUTH_TOKEN_COOKIE_NAME = "AUTH_TOKEN";
    
    /**
     * 쿠키 도메인 (A, B 서버 공유용)
     * bizmap 프로젝트와 동일하게 점 없이 설정
     */
    private static final String COOKIE_DOMAIN = "nicebizmap.co.kr";
    
    /**
     * 쿠키 경로
     */
    private static final String COOKIE_PATH = "/";
    
    /**
     * 쿠키 만료 시간 (24시간)
     */
    private static final int COOKIE_MAX_AGE = 86400; // 24시간
    
    /**
     * JWT 인증 토큰을 쿠키에 설정
     * 
     * @param response HTTP 응답 객체
     * @param token JWT 토큰
     */
    public void setAuthTokenCookie(HttpServletResponse response, String token) {
        log.info("[COOKIE] JWT 인증 토큰 쿠키 설정 시작: tokenLength={}", token.length());
        
        try {
            Cookie authCookie = new Cookie(AUTH_TOKEN_COOKIE_NAME, token);
            authCookie.setPath(COOKIE_PATH);
            authCookie.setMaxAge(COOKIE_MAX_AGE);
            authCookie.setHttpOnly(false); // XSS 공격 방지
            authCookie.setSecure(true); // HTTPS에서만 전송
            authCookie.setDomain(COOKIE_DOMAIN); // 도메인 간 공유 설정
            
            response.addCookie(authCookie);
            
            log.info("[COOKIE] JWT 인증 토큰 쿠키 설정 완료: domain={}, path={}, maxAge={}", 
                    COOKIE_DOMAIN, COOKIE_PATH, COOKIE_MAX_AGE);
            
        } catch (Exception e) {
            log.error("[COOKIE] JWT 인증 토큰 쿠키 설정 실패", e);
            throw new RuntimeException("쿠키 설정 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * JWT 인증 토큰 쿠키 제거 (로그아웃 시)
     * 
     * @param response HTTP 응답 객체
     */
    public void removeAuthTokenCookie(HttpServletResponse response) {
        log.info("[COOKIE] JWT 인증 토큰 쿠키 제거 시작");
        
        try {
            Cookie authCookie = new Cookie(AUTH_TOKEN_COOKIE_NAME, "");
            authCookie.setPath(COOKIE_PATH);
            authCookie.setMaxAge(0); // 즉시 만료
            authCookie.setHttpOnly(false);
            authCookie.setSecure(true);
            authCookie.setDomain(COOKIE_DOMAIN);
            
            response.addCookie(authCookie);
            
            log.info("[COOKIE] JWT 인증 토큰 쿠키 제거 완료");
            
        } catch (Exception e) {
            log.error("[COOKIE] JWT 인증 토큰 쿠키 제거 실패", e);
            throw new RuntimeException("쿠키 제거 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 개발환경용 쿠키 설정 (HTTP에서도 작동)
     * 
     * @param response HTTP 응답 객체
     * @param token JWT 토큰
     */
    public void setAuthTokenCookieForDevelopment(HttpServletResponse response, String token) {
        log.info("[COOKIE] 개발환경용 JWT 인증 토큰 쿠키 설정 시작: tokenLength={}", token.length());
        
        try {
            Cookie authCookie = new Cookie(AUTH_TOKEN_COOKIE_NAME, token);
            authCookie.setPath(COOKIE_PATH);
            authCookie.setMaxAge(COOKIE_MAX_AGE);
            authCookie.setHttpOnly(false);
            authCookie.setSecure(true); // 개발환경에서는 HTTP 허용
            authCookie.setDomain(COOKIE_DOMAIN);
            
            response.addCookie(authCookie);
            
            log.info("[COOKIE] 개발환경용 JWT 인증 토큰 쿠키 설정 완료: domain={}, path={}, maxAge={}", 
                    COOKIE_DOMAIN, COOKIE_PATH, COOKIE_MAX_AGE);
            
        } catch (Exception e) {
            log.error("[COOKIE] 개발환경용 JWT 인증 토큰 쿠키 설정 실패", e);
            throw new RuntimeException("쿠키 설정 중 오류가 발생했습니다.", e);
        }
    }
}
