package com.zinidata.domain.requests.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 요청 파일 VO
 * 
 * <p>요청에 첨부된 파일 정보를 관리합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestFileVO {
    
    /** 시퀀스 번호 (요청 번호) */
    private Long seq;
    
    /** 생성자 ID */
    private Long crtId;
    
    /** 서비스 구분 */
    private String serviceGb;
    
    /** 실행 여부 */
    private String executeSw;
    
    /** 파일명 */
    private String fileNm;
    
    /** 파일 경로 */
    private String filePath;
    
    /** 원본 파일명 */
    private String orgFileNm;
    
    /** 파일 크기 */
    private Long fileSize;
    
    /** 상태 */
    private String status;
    
    /** 생성 일시 */
    private LocalDateTime crtDt;
    
    /** 수정 일시 */
    private LocalDateTime updDt;

}

