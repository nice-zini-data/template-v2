package com.zinidata.domain.common.sms.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * SMS 발송 요청 DTO
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Getter
@Builder
@ToString(exclude = {"message"}) // 로그에서 메시지 내용 제외 (보안)
public class SmsRequest {
    
    /**
     * 수신자 전화번호 (필수)
     */
    private final String phoneNumber;
    
    /**
     * 메시지 내용 (필수)
     */
    private final String message;
    
    /**
     * 발신자 번호 (선택, 기본값: 1566-2122)
     */
    private final String callbackNumber;
    
    /**
     * SMS 타입 (선택, 기본값: GENERAL)
     */
    private final SmsType type;
    
    /**
     * 회원명 (선택, API 호출 시 사용)
     */
    private final String memNm;
    
    /**
     * SMS 발송 타입
     */
    public enum SmsType {
        CERTIFICATION("인증번호"),
        TEMP_PASSWORD("임시비밀번호"),
        SUBSCRIPTION("구독정보"),
        GENERAL("일반");
        
        private final String description;
        
        SmsType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 인증번호 SMS 요청 생성 (템플릿 기반)
     */
    public static SmsRequest certification(String phoneNumber, String certNumber, String template, String callbackNumber) {
        return certification(phoneNumber, certNumber, template, callbackNumber, null);
    }
    
    /**
     * 인증번호 SMS 요청 생성 (템플릿 기반, 회원명 포함)
     */
    public static SmsRequest certification(String phoneNumber, String certNumber, String template, String callbackNumber, String memNm) {
        String message = template.replace("{code}", certNumber);
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .callbackNumber(callbackNumber)
                .type(SmsType.CERTIFICATION)
                .memNm(memNm != null ? memNm : "")
                .build();
    }
    
    /**
     * 임시 비밀번호 SMS 요청 생성 (템플릿 기반)
     */
    public static SmsRequest tempPassword(String phoneNumber, String tempPassword, String template, String callbackNumber) {
        return tempPassword(phoneNumber, tempPassword, template, callbackNumber, null);
    }
    
    /**
     * 임시 비밀번호 SMS 요청 생성 (템플릿 기반, 회원명 포함)
     */
    public static SmsRequest tempPassword(String phoneNumber, String tempPassword, String template, String callbackNumber, String memNm) {
        String message = template.replace("{password}", tempPassword);
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .callbackNumber(callbackNumber)
                .type(SmsType.TEMP_PASSWORD)
                .memNm(memNm != null ? memNm : "")
                .build();
    }
    
    /**
     * 구독 정보 SMS 요청 생성 (템플릿 기반)
     */
    public static SmsRequest subscription(String phoneNumber, String reportUrl, String template, String callbackNumber) {
        return subscription(phoneNumber, reportUrl, template, callbackNumber, null);
    }
    
    /**
     * 구독 정보 SMS 요청 생성 (템플릿 기반, 회원명 포함)
     */
    public static SmsRequest subscription(String phoneNumber, String reportUrl, String template, String callbackNumber, String memNm) {
        String message = template.replace("{url}", reportUrl);
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .callbackNumber(callbackNumber)
                .type(SmsType.SUBSCRIPTION)
                .memNm(memNm != null ? memNm : "")
                .build();
    }
    
    /**
     * 일반 SMS 요청 생성
     */
    public static SmsRequest general(String phoneNumber, String message, String callbackNumber) {
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .callbackNumber(callbackNumber != null ? callbackNumber : "1566-2122")
                .type(SmsType.GENERAL)
                .memNm("")
                .build();
    }
    
    /**
     * 일반 SMS 요청 생성 (회원명 포함)
     */
    public static SmsRequest general(String phoneNumber, String message, String callbackNumber, String memNm) {
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .callbackNumber(callbackNumber != null ? callbackNumber : "1566-2122")
                .type(SmsType.GENERAL)
                .memNm(memNm != null ? memNm : "")
                .build();
    }
    
    /**
     * 입력값 검증
     */
    public boolean isValid() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty() &&
               message != null && !message.trim().isEmpty() &&
               callbackNumber != null && !callbackNumber.trim().isEmpty();
    }
}
