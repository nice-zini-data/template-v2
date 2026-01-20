package com.zinidata.domain.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프리미엄보고서 이벤트 쿠폰 생성 내역 VO
 * 
 * <p>tb_member_event 테이블 조회 결과 매핑</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCouponListVO {
    
    /**
     * 회원번호 (mem_no)
     */
    private Long memNo;
    
    /**
     * 로그인ID (login_id)
     */
    private String loginId;
    
    /**
     * 회원명 (mem_nm)
     */
    private String memNm;
    
    /**
     * 이벤트 코드 (event_code)
     */
    private String eventCode;
    
    /**
     * 유효시작일자 (start_dt)
     */
    private String startDt;
    
    /**
     * 유효종료일자 (end_dt)
     */
    private String endDt;
    
    /**
     * 생성일시 (crt_dt)
     */
    private LocalDateTime crtDt;
    
    /**
     * 생성자ID (crt_id)
     */
    private String crtId;
    
    /**
     * 변경일시 (upd_dt)
     */
    private LocalDateTime updDt;
    
    /**
     * 변경자ID (upd_id)
     */
    private String updId;
}

