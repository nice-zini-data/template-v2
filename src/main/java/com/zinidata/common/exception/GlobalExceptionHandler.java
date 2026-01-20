package com.zinidata.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.ModelAndView;

import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.enums.AuditResultStatus;
import com.zinidata.audit.service.AuditLogService;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.common.enums.Status;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditLogService auditLogService;

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        log.warn("Business Exception - Code: {}, Message: {}, URI: {}", 
                ex.getStatus().getMessageCode(), ex.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>(ex.getStatus(), ex.getMessage());
        if (ex.getData() != null) {
            response.setData(ex.getData());
        }
            
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 검증 예외 처리
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        log.warn("Validation Exception - Code: {}, Message: {}, URI: {}", 
                ex.getStatus().getMessageCode(), ex.getMessage(), request.getRequestURI());
        
        Map<String, Object> errorData = new HashMap<>();
        if (ex.hasFieldErrors()) {
            errorData.put("fieldErrors", ex.getFieldErrors());
        }
        if (ex.hasGlobalErrors()) {
            errorData.put("globalErrors", ex.getGlobalErrors());
        }
        
        ApiResponse<Object> response = new ApiResponse<>(ex.getStatus(), ex.getMessage());
        response.setData(errorData);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bean Validation 예외 처리 (@Valid, @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Method Argument Not Valid - URI: {}, Error Count: {}", 
                request.getRequestURI(), ex.getErrorCount());
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        ApiResponse<Object> response = new ApiResponse<>(Status.파라미터오류, "입력값 검증에 실패했습니다.");
        response.setData(Map.of("fieldErrors", fieldErrors));
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 파일 업로드 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        long maxSizeBytes = ex.getMaxUploadSize();
        double maxSizeMB = maxSizeBytes / 1024.0 / 1024.0;
        
        log.warn("Max Upload Size Exceeded - URI: {}, Max Size: {} bytes ({} MB)", 
                request.getRequestURI(), maxSizeBytes, String.format("%.2f", maxSizeMB));
        
        ApiResponse<Object> response = ApiResponse.badRequest(
                String.format("파일 크기가 너무 큽니다. 최대 %.0fMB까지 업로드 가능하며, 이미지 파일은 서버에서 자동으로 1MB 이하로 압축됩니다.", maxSizeMB));
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 404 에러 처리 (존재하지 않는 페이지)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        String requestUri = request.getRequestURI();
        log.warn("404 Not Found - URI: {}", requestUri);
        
        // 정적 리소스 요청이 아닌 경우에만 감사 로그 기록
        if (!isStaticResourceRequest(request)) {
            try {
                String errorMessage = auditLogService.createStandardErrorMessage("404");
                var auditLog = auditLogService.createAuditLog(
                    request,
                    null,
                    AuditActionType.PAGE_VIEW,
                    "page:" + requestUri,
                    "존재하지 않는 페이지 접근",
                    AuditResultStatus.NOT_FOUND,
                    errorMessage,
                    0L,
                    new String[0]
                );
                auditLogService.saveAuditLogAsync(auditLog);
            } catch (Exception auditEx) {
                log.error("404 감사 로그 생성 실패", auditEx);
            }
        } else {
            log.debug("정적 리소스 404 요청이므로 감사 로그에서 제외: {}", requestUri);
        }
        
        // 404 페이지 표시
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error/404");
        return modelAndView;
    }

    /**
     * 일반 예외 처리 (최종 fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected Exception - URI: {}, Message: {}", 
                request.getRequestURI(), ex.getMessage(), ex);
        
        ApiResponse<Object> response = new ApiResponse<>(Status.실패, "시스템 오류가 발생했습니다.");
        
        return ResponseEntity.internalServerError().body(response);
    }
    
    /**
     * 정적 리소스 요청인지 확인하여 감사 로그에서 제외할지 판단
     */
    private boolean isStaticResourceRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/assets/") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/favicon.ico") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".jpeg") ||
               uri.endsWith(".gif") ||
               uri.endsWith(".svg") ||
               uri.endsWith(".ico") ||
               uri.endsWith(".woff") ||
               uri.endsWith(".woff2") ||
               uri.endsWith(".ttf") ||
               uri.endsWith(".eot");
    }
} 