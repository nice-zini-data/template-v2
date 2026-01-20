package com.zinidata.common.util;

import com.zinidata.domain.common.auth.vo.MemberVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * 
 * <p>A 서버에서 B 서버로 사용자 정보를 전달하기 위한 JWT 토큰을 생성합니다.</p>
 * <p>B 서버의 JwtAuthFilter와 동일한 SECRET 키를 사용합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
public class JwtTokenUtil {
    
    /**
     * JWT 서명용 시크릿 키 (Jasypt로 복호화됨)
     * B 서버 JwtAuthFilter의 SECRET과 정확히 일치해야 함
     */
    @Value("${jwt.secret:ENC(OrLrVJv6qCFbcnjbvQ7EUkRLiOrn7bMfw/otXwRFq0as5/nc7izP0eZ9wJI9Yfz7zj2z3inrjm2TDtRSjiXeNA==)}")
    private String secret;
    
    /**
     * 토큰 만료 시간 (24시간)
     */
    private static final long EXPIRATION_TIME = 86400000; // 24시간
    
    /**
     * BetterBoss URL (환경별)
     */
    @Value("${betterboss.url}")
    private String betterBossUrl;
    
    /**
     * 일반 로그인용 JWT 토큰 생성
     * 
     * @param member 회원 정보
     * @return JWT 토큰 문자열
     */
    public String generateNormalLoginToken(MemberVO member) {
        log.info("[JWT] 일반 로그인 토큰 생성 시작: loginId={}", member.getLoginId());
        
        try {
            // BetterBoss URL에서 도메인 추출 (audience용)
            String audienceDomain = extractDomainFromUrl(betterBossUrl);
            
            String token = Jwts.builder()
                    .setSubject(member.getLoginId())
                    .setIssuer("nicebizmap.co.kr")  // 발급자 정보 추가
                    .setAudience(audienceDomain)  // 환경별 대상 서비스 정보
                    .claim("user_id", member.getLoginId())  // B 서버 필수 필드 추가
                    .claim("user_nm", member.getMemNm())
                    .claim("user_no", member.getMemNo())
                    .claim("user_type", "person")
                    .claim("mem_type", "person")  // 🔒 고정값: www.nicebizmap.co.kr 전달용
                    .claim("email_addr", member.getEmailAddr())
                    .claim("login_type", "NORMAL")  // 대문자로 변경
                    .claim("domain", "nicebizmap.co.kr")  // 도메인 정보 추가
                    .claim("target_url", betterBossUrl)  // 대상 URL 정보 추가
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .compact();
            
            log.info("[JWT] 일반 로그인 토큰 대상: audience={}, target_url={}", audienceDomain, betterBossUrl);
            
            // 디버깅을 위한 클레임 정보 로그
            log.info("[JWT] 일반 로그인 JWT 클레임 내용:");
            log.info("  - user_id: {}", member.getLoginId());
            log.info("  - user_nm: {}", member.getMemNm());
            log.info("  - user_no: {}", member.getMemNo());
            log.info("  - user_type: {}", "person");
            log.info("  - mem_type: {}", "person");
            log.info("  - email_addr: {}", member.getEmailAddr());
            log.info("  - login_type: NORMAL");
            
            log.info("[JWT] 일반 로그인 토큰 생성 완료: loginId={}, tokenLength={}", 
                    member.getLoginId(), token.length());
            
            return token;
            
        } catch (Exception e) {
            log.error("[JWT] 일반 로그인 토큰 생성 실패: loginId={}", member.getLoginId(), e);
            throw new RuntimeException("JWT 토큰 생성 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 카카오 로그인용 JWT 토큰 생성
     * 
     * @param member 회원 정보
     * @return JWT 토큰 문자열
     */
    public String generateKakaoLoginToken(MemberVO member) {
        log.info("[JWT] 카카오 로그인 토큰 생성 시작: loginId={}", member.getLoginId());
        
        try {
            // BetterBoss URL에서 도메인 추출 (audience용)
            String audienceDomain = extractDomainFromUrl(betterBossUrl);
            
            String token = Jwts.builder()
                    .setSubject(member.getLoginId())
                    .setIssuer("nicebizmap.co.kr")  // 발급자 정보 추가
                    .setAudience(audienceDomain)  // 환경별 대상 서비스 정보
                    .claim("user_id", member.getLoginId())  // B 서버 필수 필드 추가
                    .claim("user_nm", member.getMemNm())
                    .claim("user_no", member.getMemNo())
                    .claim("user_type", "person")
                    .claim("mem_type", "person")  // 🔒 고정값: www.nicebizmap.co.kr 전달용
                    .claim("email_addr", member.getEmailAddr())
                    .claim("login_type", "KAKAO")  // 대문자로 변경 (B 서버와 일치)
                    .claim("domain", "nicebizmap.co.kr")  // 도메인 정보 추가
                    .claim("target_url", betterBossUrl)  // 대상 URL 정보 추가
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .compact();
            
            log.info("[JWT] 카카오 로그인 토큰 대상: audience={}, target_url={}", audienceDomain, betterBossUrl);
            
            // 디버깅을 위한 클레임 정보 로그
            log.info("[JWT] 카카오 로그인 JWT 클레임 내용:");
            log.info("  - user_id: {}", member.getLoginId());
            log.info("  - user_nm: {}", member.getMemNm());
            log.info("  - user_no: {}", member.getMemNo());
            log.info("  - user_type: {}", "person");
            log.info("  - mem_type: {}", "person");
            log.info("  - email_addr: {}", member.getEmailAddr());
            log.info("  - login_type: KAKAO");
            
            log.info("[JWT] 카카오 로그인 토큰 생성 완료: loginId={}, tokenLength={}", 
                    member.getLoginId(), token.length());
            
            return token;
            
        } catch (Exception e) {
            log.error("[JWT] 카카오 로그인 토큰 생성 실패: loginId={}", member.getLoginId(), e);
            throw new RuntimeException("JWT 토큰 생성 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * BetterBoss URL 반환
     * 
     * @return BetterBoss URL
     */
    public String getBetterBossUrl() {
        return betterBossUrl;
    }
    
    /**
     * JWT 토큰 검증 (테스트용)
     * 
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("[JWT] 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출 (테스트용)
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("[JWT] 토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * JWT 토큰에서 로그인 타입 추출 (테스트용)
     * 
     * @param token JWT 토큰
     * @return 로그인 타입 (normal, kakao)
     */
    public String getLoginTypeFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
            return (String) claims.get("login_type");
        } catch (Exception e) {
            log.warn("[JWT] 토큰에서 로그인 타입 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * URL에서 도메인 추출
     * 
     * @param url 전체 URL
     * @return 도메인 (예: devai.nicebizmap.co.kr)
     */
    private String extractDomainFromUrl(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return "ai.nicebizmap.co.kr"; // 기본값
            }
            
            // http:// 또는 https:// 제거
            String domain = url.replaceFirst("^https?://", "");
            
            // 포트 번호나 경로 제거
            int slashIndex = domain.indexOf('/');
            if (slashIndex != -1) {
                domain = domain.substring(0, slashIndex);
            }
            
            int colonIndex = domain.indexOf(':');
            if (colonIndex != -1) {
                domain = domain.substring(0, colonIndex);
            }
            
            log.debug("[JWT] URL에서 도메인 추출: {} -> {}", url, domain);
            return domain;
            
        } catch (Exception e) {
            log.warn("[JWT] URL에서 도메인 추출 실패: {}, 기본값 사용", url, e);
            return "ai.nicebizmap.co.kr"; // 기본값
        }
    }
}
