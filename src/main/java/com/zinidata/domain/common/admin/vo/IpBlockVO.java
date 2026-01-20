package com.zinidata.domain.common.admin.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;

/**
 * IP 차단 관리 VO (ip_block 테이블 매핑)
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpBlockVO {

    /** 기본 키 */
    private Long id;

    /** IPv4/IPv6 주소 */
    @NotBlank(message = "IP 주소는 필수입니다.")
    @Size(max = 45, message = "IP 주소는 45자 이하여야 합니다.")
    private String ipAddress;

    /** 차단 사유 */
    @NotBlank(message = "차단 사유는 필수입니다.")
    @Size(max = 500, message = "차단 사유는 500자 이하여야 합니다.")
    private String blockReason;

    /** 영구 차단 여부 (true: 영구, false: 임시) */
    private Boolean isPermanent = Boolean.FALSE;

    /** 차단 시작 시간 */
    private Timestamp blockedAt;

    /** 차단 해제 시간 */
    private Timestamp unblockedAt;

    /** 임시 차단 만료 시간 */
    private Timestamp expiresAt;

    /** 프로젝트 코드 (멀티 테넌트 지원) */
    @NotBlank(message = "프로젝트 코드는 필수입니다.")
    @Size(max = 50, message = "프로젝트 코드는 50자 이하여야 합니다.")
    private String appCode = "NBZM";

    /** 차단 등록자 ID 또는 이름 */
    @NotBlank(message = "차단 등록자는 필수입니다.")
    @Size(max = 100, message = "차단 등록자는 100자 이하여야 합니다.")
    private String blockedBy;

    /** 차단 해제자 ID 또는 이름 */
    @Size(max = 100, message = "차단 해제자는 100자 이하여야 합니다.")
    private String unblockedBy;

    /** 생성 시간 */
    private Timestamp createdAt;

    /** 수정 시간 */
    private Timestamp updatedAt;

    /** 차단 상태 (ACTIVE, EXPIRED, UNBLOCKED) */
    @NotBlank(message = "차단 상태는 필수입니다.")
    @Size(max = 20, message = "차단 상태는 20자 이하여야 합니다.")
    private String status = "ACTIVE";

    // === 비즈니스 메서드 ===

    /**
     * 현재 활성 차단인지 확인
     * 
     * @return 활성 차단 여부
     */
    public boolean isActive() {
        if (!"ACTIVE".equals(status)) {
            return false;
        }
        
        // 임시 차단이고 만료 시간이 지났으면 비활성
        if (!isPermanent && expiresAt != null && expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }
        
        return true;
    }

    /**
     * 만료된 차단인지 확인
     * 
     * @return 만료 여부
     */
    public boolean isExpired() {
        return !isPermanent && expiresAt != null && expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * 남은 시간 계산 (초 단위)
     * 
     * @return 남은 시간 (초), 영구 차단이거나 이미 만료된 경우 null
     */
    public Long getRemainingSeconds() {
        if (isPermanent || expiresAt == null) {
            return null;
        }
        
        long currentTime = System.currentTimeMillis();
        if (expiresAt.getTime() < currentTime) {
            return 0L;
        }
        
        return (expiresAt.getTime() - currentTime) / 1000;
    }

    /**
     * 차단 타입 문자열 반환
     * 
     * @return 차단 타입
     */
    public String getBlockType() {
        return isPermanent ? "영구차단" : "임시차단";
    }

    /**
     * 차단 상태 한글 반환
     * 
     * @return 차단 상태 한글
     */
    public String getStatusKorean() {
        switch (status) {
            case "ACTIVE": return "차단중";
            case "EXPIRED": return "만료됨";
            case "UNBLOCKED": return "해제됨";
            default: return "알 수 없음";
        }
    }
} 