package com.zinidata.security.dto.v1;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 커스텀 사용자 상세 정보 (V1 회원 시스템)
 * 
 * <p>Spring Security UserDetails 인터페이스를 구현하여 인증된 사용자 정보를 담습니다.</p>
 * <p>세션에 저장될 핵심 회원 정보를 포함합니다.</p>
 * 
 * <h3>📋 V1 설계 원칙</h3>
 * <ul>
 *   <li><b>단일 테이블</b>: {@code tb_member} 테이블과 직접 매핑</li>
 *   <li><b>변수명 일치</b>: DB 컬럼명 → MemberVO → CustomUserDetailsV1 순으로 일치</li>
 *   <li><b>Thymeleaf 접근</b>: {@code sec:authentication="principal.필드명"} 사용</li>
 *   <li><b>단순 구조</b>: 기본적인 회원 정보만 포함</li>
 * </ul>
 * 
 * <h3>🆚 V1 vs V2 비교</h3>
 * <ul>
 *   <li><b>V1</b>: 단일 테이블 (tb_member), 기본 회원 시스템</li>
 *   <li><b>V2</b>: 멀티테넌트 (tb_members + tb_members_{corp}), 기업용 확장</li>
 * </ul>
 * 
 * <h3>📚 상세 가이드</h3>
 * <p><b>V1/V2 비교, 설계 원칙, 구현 예시 등 상세한 내용은 다음 문서를 참조하세요:</b></p>
 * <p>📖 <b>/docs/development/03-user-details-v1-v2-guide.md</b></p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 * @version 1.0 (V1 회원 시스템)
 * @see <a href="../../../../../../../../docs/development/03-user-details-v1-v2-guide.md">CustomUserDetails V1/V2 가이드</a>
 */
@Getter
@Builder
public class CustomUserDetailsV1 implements UserDetails {

    private final Long memNo;           // 회원번호 (DB: mem_no)
    private final String loginId;       // 로그인 ID (DB: login_id)
    private final String pwd;           // 암호화된 비밀번호 (DB: pwd)
    private final String emailAddr;     // 이메일 (DB: email_addr)
    private final String memNm;         // 회원명 (DB: mem_nm)
    private final String mobileNo;      // 휴대폰번호 (DB: mobile_no)
    private final String memStat;       // 회원상태 (DB: mem_stat)
    private final String memType;       // 회원타입 (DB: mem_type)
    private final String tempPwdYn;     // 임시비밀번호 여부 (DB: temp_pwd_yn)
    private final String kakaoId;       // 카카오 ID (DB: kakao_id)
    
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return pwd;
    }

    @Override
    public String getUsername() {
        return loginId;
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
    
    /**
     * Thymeleaf에서 principal.name으로 접근할 수 있도록 하는 편의 메서드
     * @return 회원명 (mem_nm)
     */
    public String getName() {
        return memNm;
    }
    
    /**
     * Thymeleaf에서 principal.email로 접근할 수 있도록 하는 편의 메서드  
     * @return 이메일 주소 (email_addr)
     */
    public String getEmail() {
        return emailAddr;
    }
    
    /**
     * 카카오 로그인 회원 여부 확인
     * @return 카카오 로그인 회원이면 true
     */
    public boolean isKakaoMember() {
        return kakaoId != null && !kakaoId.trim().isEmpty();
    }
    
    /**
     * 로그인 타입 반환 (일반/카카오)
     * @return 카카오 회원이면 "KAKAO", 일반 회원이면 "NORMAL"
     */
    public String getLoginType() {
        return isKakaoMember() ? "KAKAO" : "NORMAL";
    }
    
    /**
     * SessionRegistry에서 중복 로그인 차단을 위한 equals() 구현
     * 같은 loginId를 가진 사용자는 동일한 Principal로 인식
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CustomUserDetailsV1 that = (CustomUserDetailsV1) obj;
        return loginId != null ? loginId.equals(that.loginId) : that.loginId == null;
    }
    
    /**
     * SessionRegistry에서 중복 로그인 차단을 위한 hashCode() 구현
     * loginId를 기준으로 해시코드 생성
     */
    @Override
    public int hashCode() {
        return loginId != null ? loginId.hashCode() : 0;
    }
    
    /**
     * 디버깅을 위한 toString() 구현
     */
    @Override
    public String toString() {
        return "CustomUserDetailsV1{" +
                "memNo=" + memNo +
                ", loginId='" + loginId + '\'' +
                ", memNm='" + memNm + '\'' +
                '}';
    }
    
    // ========================================================================================
    // 📚 V2 확장 정보
    // ========================================================================================
    
    /*
     * 🔄 V2로의 확장을 고려한다면:
     * 
     * 🔗 상세 가이드: /docs/development/03-user-details-v1-v2-guide.md
     * 
     * 📋 권장 사항:
     * - V1과 V2는 완전 분리하여 관리 (현재 클래스 유지)
     * - V2는 별도 패키지에서 CustomUserDetailsV2로 신규 생성
     * - 각 프로젝트에서 필요한 버전만 선택적 사용
     * 
     * 💡 V2 주요 차이점:
     * - 멀티테넌트: tb_members + tb_members_{corpCode}
     * - 추가 필드: corpCode, tenantId, memberGroup, roles 등
     * - 복잡한 권한: 조직별, 부서별 세분화된 권한 관리
     */
}
