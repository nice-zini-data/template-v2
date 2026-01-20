package com.zinidata.domain.common.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.zinidata.security.dto.v1.CustomUserDetailsV1;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * 세션 기반 인증 필터
 * 세션에서 사용자 정보를 읽어 Spring Security 인증 객체를 복원
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 현재 인증 상태 확인
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // 세션에서 사용자 정보 조회
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Long memNo = (Long) session.getAttribute("memNo");
                    String loginId = (String) session.getAttribute("loginId");
                    String memNm = (String) session.getAttribute("memNm");
                    String emailAddr = (String) session.getAttribute("emailAddr");
                    String memType = (String) session.getAttribute("memType");
                    String memStat = (String) session.getAttribute("memStat");
                    String mobileNo = (String) session.getAttribute("mobileNo");
                    String kakaoId = (String) session.getAttribute("kakaoId");
                    
                    if (memNo != null && loginId != null) {
                        log.debug("[AUTH-V1] 세션에서 인증 정보 복원: loginId={}, memNo={}", loginId, memNo);
                        
                        // CustomUserDetailsV1 객체 생성 (Thymeleaf에서 principal.memNm 접근 가능)
                        CustomUserDetailsV1 userDetails = CustomUserDetailsV1.builder()
                                .memNo(memNo)
                                .loginId(loginId)
                                .pwd("SESSION_LOGIN")  // 세션 로그인은 비밀번호 없음
                                .emailAddr(emailAddr != null ? emailAddr : "")
                                .memNm(memNm != null ? memNm : "")
                                .mobileNo(mobileNo != null ? mobileNo : "")
                                .memStat(memStat != null ? memStat : "2")
                                .memType(memType != null ? memType : "U")
                                .kakaoId(kakaoId)  // 카카오 ID (있으면 설정)
                                .build();
                        
                        // Spring Security 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,  // CustomUserDetailsV1 객체
                                null,
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                        
                        // SecurityContext에 설정
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("[AUTH-V1] 세션 인증 필터 처리 중 오류", e);
        }
        
        filterChain.doFilter(request, response);
    }
} 