package com.zinidata.domain.common.file.exception;

/**
 * S3 서비스 예외
 * 
 * AWS S3 관련 작업에서 발생하는 예외를 처리합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
public class S3ServiceException extends RuntimeException {
    
    /**
     * 기본 생성자
     * 
     * @param message 예외 메시지
     */
    public S3ServiceException(String message) {
        super(message);
    }
    
    /**
     * 원인 예외와 함께 생성
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public S3ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인 예외만으로 생성
     * 
     * @param cause 원인 예외
     */
    public S3ServiceException(Throwable cause) {
        super(cause);
    }
}