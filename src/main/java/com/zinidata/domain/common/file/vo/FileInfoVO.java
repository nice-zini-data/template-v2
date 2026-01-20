package com.zinidata.domain.common.file.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.zinidata.common.vo.BaseVO;

/**
 * 파일 정보 VO
 * 
 * AWS S3에 저장된 파일의 정보를 관리합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FileInfoVO extends BaseVO {
    
    // ========== 기본 정보 ==========
    
    /**
     * 파일 ID (PK)
     */
    private Long fileId;
    
    /**
     * 원본 파일명
     */
    @NotBlank(message = "원본 파일명은 필수입니다")
    @Size(max = 255, message = "파일명은 255자 이하여야 합니다")
    private String originalName;
    
    /**
     * 저장된 파일명 (UUID 기반)
     */
    @NotBlank(message = "저장 파일명은 필수입니다")
    private String storedName;
    
    /**
     * S3 객체 키
     */
    @NotBlank(message = "S3 키는 필수입니다")
    private String s3Key;
    
    /**
     * S3 버킷명
     */
    @NotBlank(message = "S3 버킷명은 필수입니다")
    private String s3Bucket;
    
    /**
     * 파일 크기 (bytes)
     */
    @NotNull(message = "파일 크기는 필수입니다")
    private Long fileSize;
    
    /**
     * MIME 타입
     */
    private String mimeType;
    
    /**
     * 파일 확장자
     */
    private String extension;
    
    // ========== 접근 권한 ==========
    
    /**
     * 파일 소유자 ID
     */
    @NotNull(message = "파일 소유자는 필수입니다")
    private Long ownerId;
    
    /**
     * 접근 타입 (PRIVATE, SHARED, PUBLIC)
     */
    @NotBlank(message = "접근 타입은 필수입니다")
    private String accessType;
    
    /**
     * 공유 사용자 IDs (JSON 배열)
     */
    private String sharedUserIds;
    
    /**
     * 공유 그룹 IDs (JSON 배열)
     */
    private String sharedGroupIds;
    
    // ========== 다운로드 제한 ==========
    
    /**
     * 최대 다운로드 횟수
     */
    private Integer maxDownloads;
    
    /**
     * 현재 다운로드 횟수
     */
    @Builder.Default
    private Integer downloadCount = 0;
    
    /**
     * 파일 만료일
     */
    private LocalDateTime expiresAt;
    
    // ========== 메타데이터 ==========
    
    /**
     * 파일 설명
     */
    @Size(max = 1000, message = "파일 설명은 1000자 이하여야 합니다")
    private String description;
    
    /**
     * 파일 카테고리
     */
    private String category;
    
    /**
     * 추가 메타데이터 (JSON)
     */
    private String metadata;
    
    /**
     * 파일 상태 (ACTIVE, DELETED, ARCHIVED)
     */
    @Builder.Default
    private String status = "ACTIVE";
    
    // ========== 편의 메서드 ==========
    
    /**
     * 파일이 활성 상태인지 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    /**
     * 파일이 만료되었는지 확인
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 다운로드 제한에 도달했는지 확인
     */
    public boolean isDownloadLimitReached() {
        return maxDownloads != null && downloadCount != null && 
               downloadCount >= maxDownloads;
    }
    
    /**
     * 다운로드 횟수 증가
     */
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null) ? 1 : this.downloadCount + 1;
    }
    
    /**
     * 공개 파일인지 확인
     */
    public boolean isPublic() {
        return "PUBLIC".equals(accessType);
    }
    
    /**
     * 공유 파일인지 확인
     */
    public boolean isShared() {
        return "SHARED".equals(accessType);
    }
    
    /**
     * 개인 파일인지 확인
     */
    public boolean isPrivate() {
        return "PRIVATE".equals(accessType);
    }
}