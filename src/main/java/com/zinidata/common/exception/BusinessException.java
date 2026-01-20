package com.zinidata.common.exception;

import com.zinidata.common.enums.Status;

import lombok.Getter;

/**
 * 비즈니스 로직 처리 중 발생하는 예외
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Status status;
    private final Object data;
    
    /**
     * Status enum으로 예외 생성
     */
    public BusinessException(Status status) {
        super(status.getDesc());
        this.status = status;
        this.data = null;
    }
    
    /**
     * Status enum과 커스텀 메시지로 예외 생성
     */
    public BusinessException(Status status, String customMessage) {
        super(customMessage);
        this.status = status;
        this.data = null;
    }
    
    /**
     * Status enum과 추가 데이터로 예외 생성
     */
    public BusinessException(Status status, Object data) {
        super(status.getDesc());
        this.status = status;
        this.data = data;
    }
    
    /**
     * Status enum, 커스텀 메시지, 추가 데이터로 예외 생성
     */
    public BusinessException(Status status, String customMessage, Object data) {
        super(customMessage);
        this.status = status;
        this.data = data;
    }
    
    /**
     * Status enum과 원인 예외로 예외 생성
     */
    public BusinessException(Status status, Throwable cause) {
        super(status.getDesc(), cause);
        this.status = status;
        this.data = null;
    }
    
    /**
     * Status enum, 커스텀 메시지, 원인 예외로 예외 생성
     */
    public BusinessException(Status status, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.status = status;
        this.data = null;
    }
    
    /**
     * 단순 메시지만으로 예외 생성 (일반 실패 상태 사용)
     */
    public BusinessException(String message) {
        super(message);
        this.status = Status.실패;
        this.data = null;
    }
} 