package com.zinidata.common.vo;    

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 모든 VO의 기본 클래스
 * 
 * 생성일시, 수정일시, 생성자, 수정자 등 공통 필드를 포함합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Getter
@Setter
@ToString
public abstract class BaseVO {
    
    /**
     * 생성일시
     */
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;
    
    /**
     * 생성자 ID
     */
    private Long createdBy;
    
    /**
     * 수정자 ID
     */
    private Long updatedBy;
    
    /**
     * 삭제 여부
     */
    private Boolean deleted;
    
    /**
     * 삭제일시
     */
    private LocalDateTime deletedAt;
    
    /**
     * 삭제자 ID
     */
    private Long deletedBy;
    
    /**
     * 버전 (낙관적 잠금)
     */
    private Long version;
    
    /**
     * 생성 시 기본값 설정
     */
    public void setCreatedInfo(Long createdBy) {
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.deleted = false;
        this.version = 1L;
    }
    
    /**
     * 수정 시 기본값 설정
     */
    public void setUpdatedInfo(Long updatedBy) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
    
    /**
     * 삭제 처리 (논리적 삭제)
     */
    public void setDeletedInfo(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
} 