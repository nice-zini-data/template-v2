package com.zinidata.security.provider;

import com.zinidata.common.util.SecureHashAlgorithm;
import com.zinidata.domain.common.auth.mapper.AuthMapper;
import com.zinidata.domain.common.auth.vo.MemberVO;
import com.zinidata.security.dto.v1.CustomUserDetailsV1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 커스텀 인증 프로바이더
 * 
 * <p>Spring Security 표준 방식으로 DB 기반 사용자 인증을 처리합니다.</p>
 * <p>SHA256 해싱된 비밀번호 검증과 세션 기반 인증을 지원합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AuthMapper authMapper;

    @Value("${app.code:NBZM}")
    private String appCode;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();
        
        log.info("[AUTH_PROVIDER] 인증 요청 처리 시작: loginId={}", loginId);
        
        try {
            // 1. 입력값 검증
            if (loginId == null || loginId.trim().isEmpty()) {
                throw new BadCredentialsException("아이디를 입력해주세요.");
            }
            
            if (password == null || password.trim().isEmpty()) {
                throw new BadCredentialsException("비밀번호를 입력해주세요.");
            }
            
            // 2. 비밀번호 암호화
            String encryptedPwd = SecureHashAlgorithm.encryptSHA256(password);
            
            // 3. 회원 정보 조회
            MemberVO member = authMapper.getMember(loginId, encryptedPwd, appCode);
            
            if (member == null) {
                log.warn("[AUTH_PROVIDER] 인증 실패: loginId={}, reason=회원정보없음", loginId);
                throw new BadCredentialsException("아이디 또는 비밀번호를 확인해주세요.");
            }
            
            // 4. 사용자 상태 검증
            if (!"2".equals(member.getMemStat())) {
                log.warn("[AUTH_PROVIDER] 인증 실패: loginId={}, memStat={}", loginId, member.getMemStat());
                throw new BadCredentialsException("계정이 비활성화되었습니다. 관리자에게 문의하세요.");
            }
            
            // 5. CustomUserDetailsV1 생성
            CustomUserDetailsV1 userDetails = CustomUserDetailsV1.builder()
                    .memNo(member.getMemNo())
                    .loginId(member.getLoginId())
                    .pwd(encryptedPwd)
                    .emailAddr(member.getEmailAddr())
                    .memNm(member.getMemNm())
                    .mobileNo(member.getMobileNo())
                    .memStat(member.getMemStat())
                    .memType(member.getMemType())
                    .kakaoId(member.getKakaoId())
                    .authorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            
            log.info("[AUTH_PROVIDER] 인증 성공: memNo={}, loginId={}", member.getMemNo(), loginId);
            
            // 6. 인증 성공 토큰 반환
            return new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null,  // credentials는 보안상 null로 설정
                    userDetails.getAuthorities()
            );
            
        } catch (AuthenticationException e) {
            // 인증 예외는 그대로 재전파
            throw e;
        } catch (Exception e) {
            log.error("[AUTH_PROVIDER] 인증 처리 중 오류 발생: loginId={}", loginId, e);
            throw new BadCredentialsException("인증 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
