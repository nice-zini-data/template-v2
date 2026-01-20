package com.zinidata.common.enums;

import lombok.Getter;

/**
 * 응답 상태 코드 관리
 * 
 * <p>실제 사용하는 상태만 추가하며, 필요에 따라 확장</p>
 * <ul>
 *   <li>0000번대: 기본 처리 (성공, 실패)</li>
 *   <li>1000번대: 인증/보안 실패 상태</li>
 *   <li>2000번대: 데이터 처리 실패 상태</li>
 *   <li>3000번대: 파라미터/검증 실패 상태</li>
 *   <li>4000번대: 비즈니스 로직 실패 상태</li>
 *   <li>5000번대: 외부 연동 실패 상태</li>
 *   <li>9000번대: 시스템 오류</li>
 * </ul>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Getter
public enum Status {

    // ==================== 기본 처리 (0000번대) ====================
    성공("0000", "SUCCESS", "성공"),
    실패("9999", "FAIL", "실패"),

    // ==================== 인증/보안 실패 상태 (1000번대) ====================
    로그인실패("1001", "auth.login.FAIL", "아이디 또는 비밀번호를 확인해주세요."),
    
    // ==================== 데이터 처리 실패 상태 (2000번대) ====================
    데이터없음("2001", "data.notfound", "요청한 데이터를 찾을 수 없습니다"),
    
    // ==================== 파라미터/검증 실패 상태 (3000번대) ====================
    파라미터오류("3001", "param.error", "파라미터가 올바르지 않습니다"),
    
    // ==================== 비즈니스 로직 실패 상태 (4000번대) ====================
    아이디중복("4001", "business.duplicate.loginId", "이미 사용중인 아이디입니다."),
    이메일중복("4002", "business.duplicate.email", "이미 사용중인 이메일입니다."),
    휴대폰중복("4003", "business.duplicate.mobile", "이미 가입된 휴대폰 번호입니다."),
    카카오중복("4004", "business.duplicate.kakao", "이미 연결된 카카오 계정입니다."),
    
    // ==================== 외부 연동 실패 상태 (5000번대) ====================
    결제오류("5001", "payment.error", "결제 처리 중 오류가 발생했습니다."),
    
    // ==================== 시스템 오류 (9000번대) ====================
    시스템오류("9001", "system.error", "시스템 오류가 발생했습니다.");

    // ==================== 상수 정의 ====================
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_FAIL = "FAIL";

    // ==================== 필드 정의 ====================
    private final String result;
    private final String messageCode;
    private final String messageKey;
    private final String desc;

    /**
     * Status 생성자
     * 
     * @param messageCode 응답 코드
     * @param messageKey 메시지 키 (국제화용)
     * @param desc 상태 설명
     */
    Status(String messageCode, String messageKey, String desc) {
        this.result = messageKey.contains("SUCCESS") ? RESULT_SUCCESS : RESULT_FAIL;
        this.messageCode = messageCode;
        this.messageKey = messageKey;
        this.desc = desc;
    }

    // ==================== 편의 메서드 ====================
    
    /**
     * 성공 상태 여부 확인
     * 
     * @return 성공 시 true, 실패 시 false
     */
    public boolean isSuccess() {
        return RESULT_SUCCESS.equals(this.result);
    }

    /**
     * 실패 상태 여부 확인
     * 
     * @return 실패 시 true, 성공 시 false
     */
    public boolean isFail() {
        return RESULT_FAIL.equals(this.result);
    }

    /**
     * 인증 관련 오류 여부 확인
     * 
     * @return 인증 오류 시 true
     */
    public boolean isAuthError() {
        return messageCode.startsWith("1");
    }

    /**
     * 시스템 오류 여부 확인
     * 
     * @return 시스템 오류 시 true
     */
    public boolean isSystemError() {
        return messageCode.startsWith("9");
    }

    /**
     * 코드로 Status 찾기
     * 
     * @param code 찾을 코드
     * @return 해당하는 Status, 없으면 실패
     */
    public static Status findByCode(String code) {
        for (Status status : values()) {
            if (status.getMessageCode().equals(code)) {
                return status;
            }
        }
        return 실패;
    }
} 