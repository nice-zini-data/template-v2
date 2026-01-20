package com.zinidata.domain.requests.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 신규 설치 요청 VO
 * 
 * <p>신규 설치 요청 등록 시 사용되는 VO입니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestVO {
    
    // ==================== 요청 파라미터 ====================
    
    /** 회원 번호 (세션에서 자동 설정) */
    private Long memNo;

    /** 회원 이름 */
    private String memNm;
    
    /** 암호화된 시퀀스 번호 (URL 파라미터용) */
    private String encryptedSeq;
    
    private String address;
    private String addressDetail;
    
    // ==================== 데이터베이스 컬럼 매핑 ====================
    
    /** 시퀀스 번호 */
    private Long seq;
    
    /** 서비스 구분 */
    private String serviceGb;
    
    /** 생성자 ID */
    private Long crtId;
    
    /** 생성자 이름 */
    private String crtName;
    
    /** 생성자 전화번호 */
    private String crtPhoneNumber;

    /** 입력 받은 전화번호 */
    private String phone;
    
    /** VAN ID */
    private String vanId;
    
    /** 서비스 내용 */
    private String serviceContent;
    
    /** 설치 점포명 */
    private String installNm;
    
    /** 설치 주소 */
    private String installAddr;
    
    /** 중심 X 좌표 */
    private Double centerX;
    
    /** 중심 Y 좌표 */
    private Double centerY;
    
    /** 결제 금액 */
    private String payAmt;
    
    /** 상태 */
    private String status;
    
    /** 빠른 처리 여부 */
    private String quickSw;
    
    /** 생성 일시 */
    private LocalDateTime crtDt;
    
    /** 수정 일시 */
    private LocalDateTime updDt;
    
    // ==================== 응답 데이터 ====================
    
    /** 요청 번호 (seq와 동일, 응답용 별칭) */
    private Long requestNo;
    
    /** 등록 성공 여부 */
    private Boolean success;

    /** 페이징 */
    private long totalCount;
    private long rowNum;
    
    // ==================== 조회 파라미터 ====================
    
    /** 검색어(가맹점명, 주소) */
    private String searchText;
    
    /** 페이지 번호(1부터 시작) */
    private Integer pageNo;
    
    /** 페이지당 개수 */
    private Integer size;
    
    /** 정렬 타입(최신 순, 금액 높은 순, 거리 순) */
    private String sortType;
    
    private String columnSortType;

    // 등록일자 변환변수 yyyy-mm-dd
    private String strCrtDt;

    // ==================== 실행 정보 (tbnvps_service_execute) ====================
    
    private String encryptedExecSeq;

    /** 실행 시퀀스 번호 */
    private Long execSeq;
    
    /** 실행자 ID */
    private Long execId;
    
    /** 실행자 이름 */
    private String execName;
    
    /** 실행자 전화번호 */
    private String execPhoneNumber;

    /** 작업 예상일 */
    private String executeDate;
    
    /** 은행명 */
    private String bank;
    
    /** 계좌번호 */
    private String accountNumber;
    
    /** 예금주 */
    private String accountHolder;
    
    /** 실행 일시 */
    private LocalDateTime execDt;
    private String strExecDt;

    private String storeCallNumber;
    private String executeContent;

    /** 수행 여부 */
    private String executeSw;

}

