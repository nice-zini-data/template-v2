package com.zinidata.domain.common.user.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import com.zinidata.common.enums.UserRole;
import com.zinidata.common.vo.BaseVO;

/**
 * 사용자 정보 VO
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class UserVO extends BaseVO {
    
    // ========== 기본 정보 ==========
    
    /**
     * 사용자 ID (PK)
     */
    private Long userId;
    
    /**
     * 로그인 아이디
     */
    @NotBlank(message = "로그인 아이디는 필수입니다")
    @Size(min = 4, max = 20, message = "로그인 아이디는 4~20자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "로그인 아이디는 영문, 숫자, 언더스코어만 사용 가능합니다")
    private String loginId;
    
    /**
     * 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;
    
    /**
     * 사용자명
     */
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(max = 50, message = "사용자명은 50자 이하여야 합니다")
    private String userName;
    
    /**
     * 이메일
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;
    
    /**
     * 휴대폰 번호
     */
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;
    
    // ========== 권한 정보 ==========
    
    /**
     * 사용자 역할
     */
    @NotNull(message = "사용자 역할은 필수입니다")
    private UserRole userRole;
    
    /**
     * 계정 상태 (ACTIVE, INACTIVE, LOCKED, EXPIRED)
     */
    @NotBlank(message = "계정 상태는 필수입니다")
    private String accountStatus;
    
    /**
     * 계정 만료일
     */
    private LocalDateTime accountExpiredAt;
    
    /**
     * 비밀번호 만료일
     */
    private LocalDateTime passwordExpiredAt;
    
    /**
     * 로그인 실패 횟수
     */
    private Integer loginFailCount;
    
    /**
     * 계정 잠금 시간
     */
    private LocalDateTime accountLockedAt;
    
    // ========== 부가 정보 ==========
    
    /**
     * 최근 로그인 시간
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 최근 로그인 IP
     */
    private String lastLoginIp;
    
    /**
     * 비밀번호 변경 일시
     */
    private LocalDateTime passwordChangedAt;
    
    /**
     * 임시 비밀번호 여부
     */
    private Boolean isTemporaryPassword;
    
    /**
     * 임시 비밀번호
     */
    private String temporaryPassword;
    
    /**
     * 임시 비밀번호 만료일
     */
    private LocalDateTime temporaryPasswordExpiredAt;
    
    // ========== 개인정보 동의 ==========
    
    /**
     * 개인정보 수집 동의
     */
    private Boolean privacyAgreement;
    
    /**
     * 마케팅 정보 수신 동의
     */
    private Boolean marketingAgreement;
    
    /**
     * SMS 수신 동의
     */
    private Boolean smsAgreement;
    
    /**
     * 이메일 수신 동의
     */
    private Boolean emailAgreement;
    
    // ========== 세션 정보 ==========
    
    /**
     * 현재 세션 ID
     */
    private String sessionId;
    
    /**
     * 세션 시작 시간
     */
    private LocalDateTime sessionStartedAt;
    
    // ========== 편의 메서드 ==========
    
    /**
     * 계정 활성화 여부
     */
    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }
    
    /**
     * 계정 잠금 여부
     */
    public boolean isLocked() {
        return "LOCKED".equals(accountStatus);
    }
    
    /**
     * 계정 만료 여부
     */
    public boolean isExpired() {
        return accountExpiredAt != null && accountExpiredAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 비밀번호 만료 여부
     */
    public boolean isPasswordExpired() {
        return passwordExpiredAt != null && passwordExpiredAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 로그인 실패 횟수 초기화
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.accountLockedAt = null;
        if ("LOCKED".equals(this.accountStatus)) {
            this.accountStatus = "ACTIVE";
        }
    }
    
    /**
     * 로그인 실패 횟수 증가
     */
    public void incrementLoginFailCount() {
        this.loginFailCount = (this.loginFailCount == null) ? 1 : this.loginFailCount + 1;
    }
    
    /**
     * 계정 잠금 처리
     */
    public void lockAccount() {
        this.accountStatus = "LOCKED";
        this.accountLockedAt = LocalDateTime.now();
    }
    
    /**
     * 마지막 로그인 정보 업데이트
     */
    public void updateLastLoginInfo(String clientIp, String sessionId) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = clientIp;
        this.sessionId = sessionId;
        this.sessionStartedAt = LocalDateTime.now();
        resetLoginFailCount();
    }
} 