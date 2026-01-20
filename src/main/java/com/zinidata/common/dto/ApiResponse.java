package com.zinidata.common.dto;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zinidata.common.enums.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 표준 REST API 응답 클래스
 * 
 * <p>권장 REST API 응답 구조:</p>
 * <ul>
 *   <li>성공: success=true, code, message, data, timestamp</li>
 *   <li>실패: success=false, code, message, error, timestamp</li>
 * </ul>
 * 
 * @param <T> 응답 데이터 타입
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값 제외
public class ApiResponse<T> {

    // ==================== 필드 ====================
    
    /**
     * 성공/실패 여부
     * 필수 필드
     */
    private Boolean success;
    
    /**
     * 상태 코드 ("0000", "1010", "9999" 등)
     * 필수 필드
     */
    private String code;
    
    /**
     * 사용자 친화적 메시지
     * 필수 필드
     */
    private String message;
    
    /**
     * 성공 시 실제 데이터
     * 성공 시에만 존재
     */
    private T data;
    
    /**
     * 실패 시 에러 상세 정보
     * 실패 시에만 존재
     */
    private ErrorInfo error;
    
    /**
     * 응답 생성 시각 (ISO 8601 형식)
     * 필수 필드
     */
    private String timestamp;

    // ==================== 내부 클래스 ====================
    
    /**
     * 에러 상세 정보
     */
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        /**
         * 에러 타입 (개발자용)
         */
        private String type;
        
        /**
         * 에러 상세 정보 (개발자용)
         */
        private String details;
        
        /**
         * 유효성 검사 오류 목록
         */
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> validationErrors;
        
        public ErrorInfo() {}
        
        public ErrorInfo(String type, String details) {
            this.type = type;
            this.details = details;
        }
    }

    // ==================== 생성자 ====================
    
    public ApiResponse() {
        this.timestamp = generateTimestamp();
    }

    public ApiResponse(Status status) {
        setStatus(status);
        this.timestamp = generateTimestamp();
    }

    public ApiResponse(Status status, T data) {
        setStatus(status);
        this.data = data;
        this.timestamp = generateTimestamp();
    }

    public ApiResponse(Status status, String message) {
        setStatus(status);
        this.message = message;
        this.timestamp = generateTimestamp();
    }

    public ApiResponse(Status status, T data, String message) {
        setStatus(status);
        this.data = data;
        this.message = message;
        this.timestamp = generateTimestamp();
    }

    // ==================== 메서드 ====================

    /**
     * Status enum으로 응답 설정
     */
    public void setStatus(Status status) {
        this.success = status.isSuccess();
        this.code = status.getMessageCode();
        this.message = status.getDesc();
        
        // 실패 시 에러 정보 설정
        if (!status.isSuccess()) {
            this.error = new ErrorInfo();
            this.error.setType(status.name());
            this.error.setDetails(status.getMessageKey());
        }
    }



    /**
     * 에러 정보 설정
     */
    public void setError(String type, String details) {
        this.error = new ErrorInfo(type, details);
    }

    /**
     * 유효성 검사 오류 설정
     */
    public void setValidationErrors(List<String> validationErrors) {
        if (this.error == null) {
            this.error = new ErrorInfo();
        }
        this.error.setValidationErrors(validationErrors);
    }

    /**
     * ISO 8601 형식 타임스탬프 생성
     */
    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode("0000");
        response.setMessage("요청이 성공했습니다.");
        response.setData(data);
        return response;
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 성공 응답 생성 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode("0000");
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 일반 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(String message) {
        return error("9999", message, "GENERAL_ERROR", message);
    }

    /**
     * 커스텀 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message, String errorType, String errorDetails) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setError(errorType, errorDetails);
        return response;
    }

    /**
     * 인증 실패 응답 생성
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error("1002", message, "AUTHENTICATION_ERROR", "Authentication failed");
    }

    /**
     * 잘못된 요청 응답 생성
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error("3001", message, "VALIDATION_ERROR", "Invalid request parameters");
    }

    /**
     * 유효성 검사 실패 응답 생성
     */
    public static <T> ApiResponse<T> validationError(String message, List<String> validationErrors) {
        ApiResponse<T> response = error("3001", message, "VALIDATION_ERROR", "Validation failed");
        response.setValidationErrors(validationErrors);
        return response;
    }
}