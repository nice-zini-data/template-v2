package com.zinidata.domain.common.jwt.api;

import com.zinidata.common.util.CookieUtil;
import com.zinidata.common.util.JwtTokenUtil;
import com.zinidata.domain.common.auth.vo.MemberVO;
import com.zinidata.security.dto.v1.CustomUserDetailsV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 관리 API 컨트롤러
 * 
 * <p>JWT 토큰 생성, 검증, 쿠키 관리 등 SSO 관련 API를 제공합니다.</p>
 * <p>서버 간 Single Sign-On 연동을 위한 정식 API입니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@RestController
@RequestMapping("/api/jwt")
@RequiredArgsConstructor
@Slf4j
public class JwtManagementController {
    
    private final JwtTokenUtil jwtTokenUtil;
    private final CookieUtil cookieUtil;
    
    /**
     * 현재 로그인한 사용자의 JWT 토큰 생성 및 쿠키 설정
     * 
     * @param response HTTP 응답 객체
     * @return JWT 토큰 정보
     */
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, Object>> generateToken(HttpServletResponse response) {
        log.info("[JWT_API] JWT 토큰 생성 API 호출");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 현재 로그인한 사용자 정보 확인
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(result);
            }
            
            // CustomUserDetailsV1에서 회원 정보 추출
            if (auth.getPrincipal() instanceof CustomUserDetailsV1) {
                CustomUserDetailsV1 userDetails = (CustomUserDetailsV1) auth.getPrincipal();
                
                // MemberVO 객체 생성
                MemberVO member = new MemberVO();
                member.setLoginId(userDetails.getLoginId());
                member.setMemNm(userDetails.getMemNm());
                member.setMemNo(userDetails.getMemNo());
                member.setMemType(userDetails.getMemType());
                member.setEmailAddr(userDetails.getEmailAddr());
                member.setMobileNo(userDetails.getMobileNo());
                
                // 로그인 타입에 따라 JWT 토큰 생성
                String token;
                if (userDetails.isKakaoMember()) {
                    token = jwtTokenUtil.generateKakaoLoginToken(member);
                    log.info("[JWT_API] 카카오 로그인 JWT 토큰 생성: loginId={}", member.getLoginId());
                } else {
                    token = jwtTokenUtil.generateNormalLoginToken(member);
                    log.info("[JWT_API] 일반 로그인 JWT 토큰 생성: loginId={}", member.getLoginId());
                }
                
                // 쿠키에 토큰 설정
                cookieUtil.setAuthTokenCookie(response, token);
                
                result.put("success", true);
                result.put("message", "JWT 토큰이 생성되고 쿠키에 설정되었습니다.");
                result.put("tokenLength", token.length());
                result.put("loginId", member.getLoginId());
                result.put("memNm", member.getMemNm());
                result.put("loginType", userDetails.getLoginType());
                
                log.info("[JWT_API] JWT 토큰 생성 완료: loginId={}, loginType={}", 
                        member.getLoginId(), userDetails.getLoginType());
                
            } else {
                result.put("success", false);
                result.put("message", "사용자 정보를 가져올 수 없습니다.");
                return ResponseEntity.status(400).body(result);
            }
            
        } catch (Exception e) {
            log.error("[JWT_API] JWT 토큰 생성 실패", e);
            result.put("success", false);
            result.put("message", "JWT 토큰 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * JWT 토큰 검증 API
     * 
     * @param token JWT 토큰
     * @return 토큰 검증 결과
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        log.info("[JWT_API] JWT 토큰 검증 API 호출");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isValid = jwtTokenUtil.validateToken(token);
            String userId = jwtTokenUtil.getUserIdFromToken(token);
            String loginType = jwtTokenUtil.getLoginTypeFromToken(token);
            
            result.put("success", true);
            result.put("isValid", isValid);
            result.put("userId", userId);
            result.put("loginType", loginType);
            result.put("message", isValid ? "토큰이 유효합니다." : "토큰이 유효하지 않습니다.");
            
            log.info("[JWT_API] JWT 토큰 검증 완료: isValid={}, userId={}", isValid, userId);
            
        } catch (Exception e) {
            log.error("[JWT_API] JWT 토큰 검증 실패", e);
            result.put("success", false);
            result.put("message", "토큰 검증 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * JWT 토큰 쿠키 제거 API
     * 
     * @param response HTTP 응답 객체
     * @return 쿠키 제거 결과
     */
    @PostMapping("/remove-token")
    public ResponseEntity<Map<String, Object>> removeToken(HttpServletResponse response) {
        log.info("[JWT_API] JWT 토큰 쿠키 제거 API 호출");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            cookieUtil.removeAuthTokenCookie(response);
            
            result.put("success", true);
            result.put("message", "JWT 토큰 쿠키가 제거되었습니다.");
            
            log.info("[JWT_API] JWT 토큰 쿠키 제거 완료");
            
        } catch (Exception e) {
            log.error("[JWT_API] JWT 토큰 쿠키 제거 실패", e);
            result.put("success", false);
            result.put("message", "쿠키 제거 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
        
        return ResponseEntity.ok(result);
    }
}
