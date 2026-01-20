package com.zinidata.common.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

import com.zinidata.common.enums.Status;

/**
 * 유효성 검사 실패 시 발생하는 예외
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Getter
public class ValidationException extends RuntimeException {
    
    private final Status status;
    private final Map<String, String> fieldErrors;
    private final List<String> globalErrors;
    
    /**
     * Status enum으로 검증 예외 생성
     */
    public ValidationException(Status status) {
        super(status.getDesc());
        this.status = status;
        this.fieldErrors = Map.of();
        this.globalErrors = List.of();
    }
    
    /**
     * Status enum과 커스텀 메시지로 검증 예외 생성
     */
    public ValidationException(Status status, String customMessage) {
        super(customMessage);
        this.status = status;
        this.fieldErrors = Map.of();
        this.globalErrors = List.of();
    }
    
    /**
     * Status enum과 필드 오류로 검증 예외 생성
     */
    public ValidationException(Status status, Map<String, String> fieldErrors) {
        super(status.getDesc());
        this.status = status;
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
        this.globalErrors = List.of();
    }
    
    /**
     * Status enum과 전역 오류로 검증 예외 생성
     */
    public ValidationException(Status status, List<String> globalErrors) {
        super(status.getDesc());
        this.status = status;
        this.fieldErrors = Map.of();
        this.globalErrors = globalErrors != null ? globalErrors : List.of();
    }
    
    /**
     * Status enum과 필드/전역 오류로 검증 예외 생성
     */
    public ValidationException(Status status, Map<String, String> fieldErrors, List<String> globalErrors) {
        super(status.getDesc());
        this.status = status;
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
        this.globalErrors = globalErrors != null ? globalErrors : List.of();
    }
    
    /**
     * Status enum, 커스텀 메시지, 필드 오류로 검증 예외 생성
     */
    public ValidationException(Status status, String customMessage, Map<String, String> fieldErrors) {
        super(customMessage);
        this.status = status;
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
        this.globalErrors = List.of();
    }
    
    /**
     * 필드 오류 존재 여부 확인
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
    
    /**
     * 전역 오류 존재 여부 확인
     */
    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
    }
    
    /**
     * 오류 존재 여부 확인
     */
    public boolean hasAnyErrors() {
        return hasFieldErrors() || hasGlobalErrors();
    }
} 