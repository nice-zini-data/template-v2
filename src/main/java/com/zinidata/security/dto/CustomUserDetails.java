package com.zinidata.security.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 커스텀 사용자 상세 정보 (멀티테넌트 시스템)
 * 
 * <p>Spring Security UserDetails 인터페이스를 구현하여 멀티테넌트 환경의 인증된 사용자 정보를 담습니다.</p>
 * <p>기업용 확장 기능과 세분화된 권한 관리를 포함합니다.</p>
 * 
 * <h3>🏢 설계 원칙</h3>
 * <ul>
 *   <li><b>멀티테넌트</b>: {@code tb_members + tb_members_{corpCode}} 구조</li>
 *   <li><b>테넌트 격리</b>: 고객사별 완전 분리된 데이터 관리</li>
 *   <li><b>확장 권한</b>: 조직, 부서, 역할별 세분화된 권한 체계</li>
 *   <li><b>보안 강화</b>: IP 제한, 로그인 실패 제어, 세션 관리</li>
 * </ul>
 * 
 * <h3>📚 상세 가이드</h3>
 * <p><b>구현 예시, 마이그레이션 방법 등 상세한 내용은 다음 문서를 참조하세요:</b></p>
 * <p>📖 <b>/docs/development/03-user-details-guide.md</b></p>
 * 
 * @author ZiniData 개발팀
 * @since 2.0
 * @version 2.0 (멀티테넌트 시스템)
 * @see <a href="../../../../../../../../docs/development/03-user-details-guide.md">CustomUserDetails 가이드</a>
 */
@Getter
@Builder
public class CustomUserDetails implements UserDetails {

    // ================================================================================================
    // TODO: 필드들을 실제 테이블 구조에 맞게 정의 필요
    // ================================================================================================
    
    /*
     * 멀티테넌트 시스템에서 사용할 필드들을 여기에 정의하세요.
     * 
     * 예시 구조:
     * - tb_members 테이블의 공통 필드들
     * - tb_members_{corpCode} 테이블의 확장 필드들
     * - 실제 요구사항에 맞는 비즈니스 필드들
     * 
     * 주의: 실제 DB 스키마 확인 후 필드 추가할 것
     */
    
    // ================================================================================================
    // 🔧 Spring Security 필수 필드
    // ================================================================================================
    
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    // ================================================================================================
    // 🔧 Spring Security 인터페이스 구현
    // ================================================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // TODO: 실제 비밀번호 필드명으로 변경 필요
        return null;
    }

    @Override
    public String getUsername() {
        // TODO: 실제 로그인ID 필드명으로 변경 필요
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // ================================================================================================
    // 🎨 Thymeleaf 편의 메서드 (TODO: 실제 필드 추가 후 구현)
    // ================================================================================================
    
    /**
     * Thymeleaf에서 principal.name으로 접근할 수 있도록 하는 편의 메서드
     * @return 회원명
     */
    public String getName() {
        // TODO: 실제 회원명 필드로 변경 필요
        return null;
    }
    
    /**
     * Thymeleaf에서 principal.email로 접근할 수 있도록 하는 편의 메서드  
     * @return 이메일 주소
     */
    public String getEmail() {
        // TODO: 실제 이메일 필드로 변경 필요
        return null;
    }
    
    // ================================================================================================
    // 🚀 비즈니스 메서드 (TODO: 실제 필드 추가 후 구현)
    // ================================================================================================
    
    /*
     * 전용 비즈니스 메서드들을 여기에 추가하세요.
     * 
     * 예시:
     * - 멀티테넌트 관련 메서드
     * - 권한 체크 메서드  
     * - 조직/부서 관련 메서드
     * - 보안 강화 메서드
     * 
     * 주의: 실제 필드 정의 후 구현할 것
     */
}
