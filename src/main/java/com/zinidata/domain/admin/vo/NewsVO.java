package com.zinidata.domain.admin.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 뉴스 기사 VO
 * 
 * <p>관리자 뉴스 기사 데이터를 담는 Value Object입니다.</p>
 * <p>Toast UI Editor로 작성된 HTML/Markdown 컨텐츠를 저장하며,
 * 발행 상태(draft/published/archived) 관리를 지원합니다.</p>
 * 
 * <h3>상태(status) 정의</h3>
 * <ul>
 *   <li><strong>draft</strong>: 임시저장 (작성 중)</li>
 *   <li><strong>published</strong>: 발행됨 (공개)</li>
 *   <li><strong>archived</strong>: 보관됨 (비공개 전환)</li>
 * </ul>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 * @see com.zinidata.domain.admin.mapper.NewsMapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsVO {
    
    /**
     * 뉴스 ID (Primary Key, BIGSERIAL)
     */
    private Long newsId;
    
    /**
     * 제목 (최대 200자)
     */
    private String title;
    
    /**
     * 요약 (최대 500자)
     * 목록 화면 표시용
     */
    private String summary;
    
    /**
     * 상태 (draft, published, archived)
     * 기본값: draft
     */
    private String status;
    
    /**
     * 본문 내용 (HTML)
     * Toast UI Editor에서 변환된 HTML 컨텐츠
     * 실제 사용자 화면에 표시되는 내용
     */
    private String contentHtml;
    
    /**
     * 본문 내용 (Markdown)
     * Toast UI Editor의 원본 Markdown 소스
     * 관리자 수정 시 재편집용
     */
    private String contentMarkdown;
    
    /**
     * 발행 예정일 (DATE)
     * 예약 발행 기능에 활용
     */
    private LocalDate publishDate;
    
    /**
     * 실제 발행 일시 (TIMESTAMP)
     * status가 'published'로 변경된 시점
     */
    private LocalDateTime publishedAt;
    
    /**
     * 조회수
     * 기본값: 0
     */
    private Integer viewCount;
    
    /**
     * 좋아요 수
     * 기본값: 0
     */
    private Integer likeCount;
    
    /**
     * 작성자 ID (VARCHAR 50)
     * 관리자 계정 ID
     */
    private String authorId;
    
    /**
     * 작성자 이름 (VARCHAR 100)
     * 화면 표시용
     */
    private String authorName;
    
    /**
     * 상단 고정 여부
     * true: 목록 최상단 고정
     * 기본값: false
     */
    private Boolean isPinned;
    
    /**
     * 활성화 여부
     * false: 숨김 처리 (논리적 삭제)
     * 기본값: true
     */
    private Boolean isActive;
    
    /**
     * 생성일시 (감사 정보)
     * 기본값: CURRENT_TIMESTAMP
     */
    private LocalDateTime createdAt;
    
    /**
     * 생성자 ID (감사 정보)
     */
    private String createdBy;
    
    /**
     * 수정일시 (감사 정보)
     */
    private LocalDateTime updatedAt;
    
    /**
     * 수정자 ID (감사 정보)
     */
    private String updatedBy;
    
    // ==================== 추가 필드 (페이징용) ====================
    
    /**
     * 행 번호 (페이징용)
     * SELECT 쿼리의 ROW_NUMBER() 결과
     */
    private Integer rowNum;
    
    /**
     * 전체 행 수 (페이징용)
     * SELECT 쿼리의 COUNT(*) OVER() 결과
     */
    private Integer totalRowCount;
}

