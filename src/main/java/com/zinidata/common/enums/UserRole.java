package com.zinidata.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한 역할 열거형
 * 
 * 계층적 권한 시스템을 구현합니다.
 * 높은 레벨의 권한은 낮은 레벨의 권한을 포함합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    
    /**
     * 최고관리자 (레벨 1000)
     * 사이트 관리 + 시스템 관리
     * - 시스템 모니터링, 서버 관리, 데이터베이스 관리
     * - 사이트 전체 설정 및 운영 관리
     * - 모든 권한 보유
     */
    SUPER_ADMIN("최고관리자", 1000, "ROLE_SUPER_ADMIN"),
    
    /**
     * 관리자 (레벨 800)
     * 사이트 관리
     * - 사이트 운영 및 컨텐츠 관리
     * - 사용자 관리 (전체 회원 관리)
     * - 사이트 설정 관리
     */
    ADMIN("관리자", 800, "ROLE_ADMIN"),
    
    /**
     * 매니저 (레벨 600)
     * 일반 회원 관리
     * - 고객사 서비스에서 해당 서비스의 일반회원 승인 관리
     * - 일반회원의 사이트 사용 허용/차단 관리
     * - 제한된 관리 기능 (본인 담당 영역만)
     */
    MANAGER("매니저", 600, "ROLE_MANAGER"),
    
    /**
     * 일반사용자 (레벨 400)
     * 일반회원
     * - 기본적인 서비스 이용
     * - 개인 정보 관리
     * - 서비스별 기능 이용
     */
    USER("일반사용자", 400, "ROLE_USER"),
    
    /**
     * 게스트 (레벨 200)
     * 비회원
     * - 공개 컨텐츠 조회
     * - 회원가입 및 로그인 기능
     * - 제한적인 서비스 체험
     */
    GUEST("게스트", 200, "ROLE_GUEST");
    
    /**
     * 역할 한국어 설명
     */
    private final String description;
    
    /**
     * 권한 레벨 (높을수록 상위 권한)
     */
    private final int level;
    
    /**
     * Spring Security 역할명 (ROLE_ 접두사 포함)
     */
    private final String authority;
    
    /**
     * 현재 권한이 지정된 권한 레벨 이상인지 확인
     * 
     * @param requiredRole 필요한 권한
     * @return 권한 충족 여부
     */
    public boolean hasAuthority(UserRole requiredRole) {
        return this.level >= requiredRole.level;
    }
    
    /**
     * 시스템 관리자 권한 확인 (SUPER_ADMIN)
     */
    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }
    
    /**
     * 사이트 관리자 권한 확인 (ADMIN 이상)
     */
    public boolean isAdmin() {
        return this.level >= ADMIN.level;
    }
    
    /**
     * 매니저 권한 확인 (MANAGER 이상)
     */
    public boolean isManager() {
        return this.level >= MANAGER.level;
    }
    
    /**
     * 일반 회원 권한 확인 (USER 이상)
     */
    public boolean isUser() {
        return this.level >= USER.level;
    }
    
    /**
     * 게스트 권한 확인 (모든 사용자)
     */
    public boolean isGuest() {
        return this.level >= GUEST.level;
    }
    
    /**
     * 회원 승인 관리 권한 확인 (MANAGER 이상)
     */
    public boolean canManageUserApproval() {
        return this.level >= MANAGER.level;
    }
    
    /**
     * 사이트 관리 권한 확인 (ADMIN 이상)
     */
    public boolean canManageSite() {
        return this.level >= ADMIN.level;
    }
    
    /**
     * 시스템 관리 권한 확인 (SUPER_ADMIN만)
     */
    public boolean canManageSystem() {
        return this == SUPER_ADMIN;
    }
    
    /**
     * 코드명으로 UserRole 찾기
     * 
     * @param name 역할명
     * @return UserRole (없으면 GUEST)
     */
    public static UserRole fromName(String name) {
        if (name == null) return GUEST;
        
        try {
            return UserRole.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GUEST;
        }
    }
    
    /**
     * Spring Security Authority명으로 UserRole 찾기
     * 
     * @param authority Spring Security 권한명
     * @return UserRole (없으면 GUEST)
     */
    public static UserRole fromAuthority(String authority) {
        if (authority == null) return GUEST;
        
        for (UserRole role : values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        return GUEST;
    }
} 