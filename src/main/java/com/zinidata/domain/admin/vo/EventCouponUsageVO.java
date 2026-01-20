package com.zinidata.domain.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프리미엄보고서 이벤트 쿠폰 참여 내역 VO
 * 
 * <p>tb_sales + tb_member_event + tb_member 조인 조회 결과 매핑</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCouponUsageVO {
    
    /**
     * 구매자 회원번호 (buyer_no)
     */
    private Long buyerNo;
    
    /**
     * 구매자 로그인ID (buyer_id)
     */
    private String buyerId;
    
    /**
     * 구매자명 (buyer_nm)
     */
    private String buyerNm;
    
    /**
     * 사용된 이벤트 코드 (event_code)
     */
    private String eventCode;
    
    /**
     * 쿠폰 소유자 회원번호 (owner_mem_no)
     */
    private Long ownerMemNo;
    
    /**
     * 쿠폰 소유자 로그인ID (owner_login_id)
     */
    private String ownerLoginId;
    
    /**
     * 쿠폰 소유자명 (owner_mem_nm)
     */
    private String ownerMemNm;
    
    /**
     * 유효시작일시 (valid_dt_from)
     */
    private LocalDateTime validDtFrom;
    
    /**
     * 유효종료일시 (valid_dt_to)
     */
    private LocalDateTime validDtTo;
    
    /**
     * 판매번호 (sale_no)
     */
    private Long salesNo;
}

