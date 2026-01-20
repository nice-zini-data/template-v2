package com.zinidata.domain.requests.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 맵 조회 결과 VO
 * 
 * <p>요청 맵 조회 시 사용되는 VO입니다.</p>
 * <p>block, admi, cty, mega 구분에 따라 다른 데이터를 반환합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapVO {
    
    // ==================== 요청 파라미터 ====================
    
    /** 구분 (block, admi, cty, mega) */
    private String gubun;
    
    /** 최소 X 좌표 */
    private Double minx;
    
    /** 최소 Y 좌표 */
    private Double miny;
    
    /** 최대 X 좌표 */
    private Double maxx;
    
    /** 최대 Y 좌표 */
    private Double maxy;
    
    // ==================== 공통 필드 ====================
    
    /** 중심 X 좌표 */
    private Double centerX;
    
    /** 중심 Y 좌표 */
    private Double centerY;

    private Double lng;
    
    private Double lat;
    
    /** 코드 (admi_cd, cty_cd, mega_cd) */
    private String cd;
    
    /** 이름 (admi_nm, cty_nm, mega_nm) */
    private String nm;
    
    /** 개수 */
    private Long cnt;
    
    // ==================== block 구분 시 사용 (tbnvps_service_request 전체 컬럼) ====================
    
    /** 시퀀스 번호 */
    private Long seq;
    
    /** 암호화된 시퀀스 번호 (URL 파라미터용) */
    private String encryptedSeq;
    
    /** 서비스 구분 */
    private String serviceGb;
    
    /** 설치 점포명 */
    private String installNm;
    
    /** 설치 주소 */
    private String installAddr;
    
    /** 결제 금액 */
    private String payAmt;
    
    /** 상태 */
    private String status;
    
    /** 생성 일시 */
    private LocalDateTime crtDt;

    private long distance;
    
}
