package com.zinidata.domain.home.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 홈 통계 정보 VO
 * 
 * <p>반경 내 서비스 요청 수, 오늘 서비스 요청 수, 사용자별 요청/실행 수를 포함합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatsVO {
    
    // ==================== 요청 파라미터 ====================
    
    /** 회원 번호 */
    private String memNo;
    
    /** 중심점 X 좌표 (경도) */
    private Double centerX;
    
    /** 중심점 Y 좌표 (위도) */
    private Double centerY;
    
    /** 반경 (미터) */
    private Integer radius;
    
    // ==================== 응답 데이터 ====================
    
    /** 반경 내 서비스 요청 수 */
    private Integer radiusCnt;
    
    /** 오늘 서비스 요청 수 */
    private Integer todayCnt;
    
    /** 사용자별 요청 수 (status='0') */
    private Integer requestCnt;
    
    /** 사용자별 실행 수 (status='1') */
    private Integer execCnt;
}
