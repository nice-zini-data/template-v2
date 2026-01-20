package com.zinidata.domain.mypage.vo;

import lombok.Data;

/**
 * TB_USAGE_ZONE 테이블 VO
 * 
 * <p>프리미엄 보고서 이용 내역 정보를 담는 VO입니다.</p>
 * <p>분석 번호, 사용 일시, 지역 정보, 상품 정보, 처리 상태, 잔여 일수 등을 포함합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
public class UsageZoneVO {
    
    /**
     * 분석 번호
     */
    private String analNo;
    
    /**
     * 행정동 코드
     */
    private String admiCd;
    
    /**
     * 업종3 코드
     */
    private String upjong3Cd;
    
    /**
     * 년월 (YYYYMM)
     */
    private String yyyymm;
    
    /**
     * 클라이언트 IP
     */
    private String clientIp;
    
    /**
     * 생성자 ID (로그인 ID)
     */
    private String crtId;
    
    /**
     * 생성자명 (회원명)
     */
    private String memNm;
    
    /**
     * 생성 일시
     */
    private String crtDt;
    
    /**
     * 수정자 ID
     */
    private String updId;
    
    /**
     * 수정 일시
     */
    private String updDt;
    
    /**
     * 사용 일자 (YYYY-MM-DD)
     */
    private String usageDate;
    
    /**
     * 사용 일시 (YYYY-MM-DD HH:MI)
     */
    private String usageDt;
    
    /**
     * 상권명
     */
    private String zoneNm;
    
    /**
     * 서비스 타입 (betterboss, nicebizmap)
     */
    private String serviceType;
    
    /**
     * 활성화 상태 (Y: 활성, N: 비활성)
     */
    private String isActive;
    
    /**
     * 확장분석 여부 (NULL: 일반분석, 1: 확장분석)
     */
    private String extendYn;
    
    /**
     * 시도명
     */
    private String megaNm;
    
    /**
     * 시군구명
     */
    private String ctyNm;
    
    /**
     * 읍면동명
     */
    private String admiNm;
    
    /**
     * 업종1명 (대분류)
     */
    private String upjong1Nm;
    
    /**
     * 업종2명 (중분류)
     */
    private String upjong2Nm;
    
    /**
     * 업종3명 (소분류)
     */
    private String upjong3Nm;
    
    /**
     * 잔여 일수
     */
    private Integer laveDays;
    
    /**
     * 상품 타입
     */
    private String prodType;
    
    /**
     * 상품 ID (여러 상품인 경우 콤마로 구분)
     */
    private String prodId;
    
    /**
     * 상품명 (여러 상품인 경우 콤마로 구분)
     */
    private String prodNm;
    
    /**
     * 처리 상태 (01:준비(생성전), 02:시작(생성중), 03:에러, 04:완료)
     */
    private String procStat;
    
    /**
     * 처리 플래그 (0:일반, 1:30분내 업데이트)
     */
    private Integer procFlag;
    
    /**
     * 행 번호 (페이징용)
     */
    private Long rn;
    
    /**
     * 전체 행 수 (페이징용)
     */
    private Long totalRowCount;
    
    /**
     * 처리 상태명 (화면 표시용)
     */
    public String getProcStatName() {
        if (procStat == null) return "알 수 없음";
        
        switch (procStat) {
            case "01": return "준비중"; // 준비(생성전) - 로딩바 🟡 로딩바 (황색 스피너)
            case "02": return "생성중"; // 시작(생성중) - 로딩바 🔵 로딩바 (파란색 스피너)
            case "03": return "분석실패"; // 에러 - 다시분석하기 버튼 🔴 빨간색 도트
            case "04": return "완료"; // 완료 - 화면보기|보고서출력 버튼 🟢 초록색 도트
            default: return "알 수 없음";
        }
    }
    
    /**
     * 잔여 일수 표시 텍스트
     */
    public String getLaveDaysText() {
        if (laveDays == null || laveDays <= 0) {
            return "만료";
        }
        return laveDays + "일 남음";
    }
    
    /**
     * 상권 전체 주소
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (megaNm != null) address.append(megaNm).append(" ");
        if (ctyNm != null) address.append(ctyNm).append(" ");
        if (admiNm != null) address.append(admiNm).append(" ");
        if (zoneNm != null) address.append(zoneNm);
        return address.toString().trim();
    }
    
    /**
     * 업종 전체명
     */
    public String getFullUpjongName() {
        StringBuilder upjong = new StringBuilder();
        if (upjong1Nm != null) upjong.append(upjong1Nm);
        if (upjong2Nm != null) upjong.append(" > ").append(upjong2Nm);
        if (upjong3Nm != null) upjong.append(" > ").append(upjong3Nm);
        return upjong.toString();
    }
    
    /**
     * 다운로드 가능 여부 (완료 상태이고 만료되지 않은 경우만)
     */
    public boolean isDownloadable() {
        // 완료 상태(04)이고, 잔여일수가 있는 경우만 다운로드 가능
        boolean isCompleted = "04".equals(procStat);
        boolean isNotExpired = laveDays != null && laveDays > 0;
        return isCompleted && isNotExpired;
    }
    
    /**
     * 보고서 보기 가능 여부 (완료 상태이고 만료되지 않은 경우만)
     */
    public boolean isViewable() {
        // 완료 상태(04)이고, 잔여일수가 있는 경우만 보기 가능
        return isDownloadable();
    }
    
    /**
     * 다시 분석 가능 여부 (에러 상태인 경우만)
     */
    public boolean isRetryable() {
        // 에러 상태(03)인 경우만 다시 분석 가능
        return "03".equals(procStat);
    }
    
    /**
     * 로딩 상태 여부 (준비중 또는 생성중)
     */
    public boolean isProcessing() {
        // 준비중(01) 또는 생성중(02) 상태
        return "01".equals(procStat) || "02".equals(procStat);
    }
}
