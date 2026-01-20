package com.zinidata.domain.mypage.vo;

import lombok.Data;

/**
 * 상품 정보 VO
 * 
 * <p>마이페이지에서 표시할 상품 정보를 담는 클래스입니다.</p>
 * <p>SQL 쿼리 결과와 매핑됩니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
public class ProductVO {
    
    /**
     * 판매 번호
     */
    private String saleNo;
    
    /**
     * 상품 ID
     */
    private String prodId;
    
    /**
     * 상품 ID 목록 (콤마로 구분)
     */
    private String prodIdS;
    
    /**
     * 상품 ID 목록 (JSON 형태)
     */
    private String prodIdS2;
    
    /**
     * 상품명
     */
    private String prodNm;
    
    /**
     * 상품 타입
     */
    private String prodType;
    
    /**
     * 총 제한 횟수
     */
    private Integer totLimit;
    
    /**
     * 사용 횟수
     */
    private Integer useLimit;
    
    /**
     * 남은 횟수
     */
    private Integer remainLimit;
    
    /**
     * 판매 금액
     */
    private Long saleAmt;
    
    /**
     * 유효 시작일 (YYYYMMDDHH24MI 형식)
     */
    private String validDtFrom;
    
    /**
     * 유효 종료일 (YYYYMMDDHH24MI 형식)
     */
    private String validDtTo;
    
    /**
     * 전체 행 개수 (페이징용)
     */
    private long totalRowCount;
    
    /**
     * 남은 시간 (일)
     */
    private Long hour;
    
    /**
     * 남은 시간 (분)
     */
    private Long min;
    
    /**
     * 남은 시간 (초)
     */
    private Long sec;
    
    /**
     * 순위
     */
    private Integer rank;
    
    /**
     * 시작일 (YYYYMMDDHH24MISS 형식)
     */
    private String dateFrom;
    
    /**
     * 종료일 (YYYYMMDDHH24MISS 형식)
     */
    private String dateTo;
    
    /**
     * 결제 일시 (YYYYMMDDHH24MISS 형식)
     */
    private String payDtm;
    
    /**
     * 결제 일시 (포맷된 형식: YYYY-MM-DD HH24:MI)
     */
    private String payDtmStr;
    
    /**
     * 결제 방법
     */
    private String payMethod;
    
    // ================================
    // 편의 메서드
    // ================================
    
    /**
     * 상품명에 사용 횟수 정보를 포함한 문자열 반환
     * 
     * @return "상품명 (남은횟수 / 총횟수)" 형식
     */
    public String getProdNmWithLimit() {
        if (prodNm == null || totLimit == null || remainLimit == null) {
            return prodNm;
        }
        return String.format("%s (%d / %d)", prodNm, remainLimit, totLimit);
    }
    
    /**
     * 유효기간을 "시작일 ~ 종료일" 형식으로 반환
     * 
     * @return 유효기간 문자열
     */
    public String getValidPeriod() {
        if (validDtFrom == null || validDtTo == null) {
            return "";
        }
        
        // YYYYMMDDHH24MI 형식을 YYYY-MM-DD 형식으로 변환
        String fromDate = formatDate(validDtFrom);
        String toDate = formatDate(validDtTo);
        
        return String.format("%s ~ %s", fromDate, toDate);
    }
    
    /**
     * 금액을 천단위 콤마 형식으로 반환
     * 
     * @return 포맷된 금액 문자열
     */
    public String getFormattedSaleAmt() {
        if (saleAmt == null) {
            return "0원";
        }
        return String.format("%,d원", saleAmt);
    }
    
    /**
     * 날짜 문자열을 YYYY-MM-DD 형식으로 변환
     * 
     * @param dateStr YYYYMMDDHH24MI 형식의 날짜 문자열
     * @return YYYY-MM-DD 형식의 날짜 문자열
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() < 8) {
            return dateStr;
        }
        
        String year = dateStr.substring(0, 4);
        String month = dateStr.substring(4, 6);
        String day = dateStr.substring(6, 8);
        
        return String.format("%s-%s-%s", year, month, day);
    }
    
    /**
     * 포맷된 결제 일시 반환 (결제 내역용)
     * 
     * @return 포맷된 결제 일시 또는 기본값
     */
    public String getFormattedPayDate() {
        return payDtmStr != null ? payDtmStr : "날짜 정보 없음";
    }
}
