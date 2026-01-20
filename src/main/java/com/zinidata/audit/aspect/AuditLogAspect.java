package com.zinidata.audit.aspect;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.enums.AuditResultStatus;
import com.zinidata.audit.service.AuditLogService;
import com.zinidata.audit.vo.AuditLogVO;
import com.zinidata.common.dto.ApiResponse;

import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 감사 로그 AOP 처리 클래스
 * 
 * <p>{@code @AuditLog} 어노테이션이 적용된 메서드의 실행 전후로 감사 로그를 자동 생성합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {
    
    private final AuditLogService auditLogService;
    
    /**
     * @AuditLog 어노테이션이 적용된 메서드를 Around 방식으로 감싸서 처리
     */
    @Around("@annotation(com.zinidata.audit.annotation.AuditLog)")
    public Object logAuditInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        
        long startTime = System.currentTimeMillis();
        
        // 메서드 정보 및 어노테이션 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);
        
        // HTTP 요청 정보 추출
        HttpServletRequest request = getCurrentHttpRequest();
        
        // 웹 컨텍스트 외부에서 실행된 경우 처리
        if (request == null) {
            log.debug("HTTP 요청 정보를 찾을 수 없습니다. 웹 컨텍스트 외부에서 실행되었을 수 있습니다.");
            return joinPoint.proceed();
        }
        
        // 정적 리소스 요청은 감사 로그에서 제외
        if (isStaticResourceRequest(request)) {
            log.debug("정적 리소스 요청이므로 감사 로그에서 제외: {}", request.getRequestURI());
            return joinPoint.proceed();
        }
        
        Object result = null;
        try {
            // 실제 메서드 실행
            result = joinPoint.proceed();
            
            // 응답 결과에 따른 로그 기록
            ApiResponse<?> apiResponse = extractApiResponse(result);
            
            if (apiResponse != null) {
                // ApiResponse인 경우 success 필드로 실제 성공/실패 판단
                if (apiResponse.getSuccess()) {
                    // 실제 성공
                    if (auditLogAnnotation.logOnSuccess()) {
                        logAuditEvent(
                            request,
                            joinPoint.getArgs(),
                            auditLogAnnotation,
                            AuditResultStatus.SUCCESS,
                            null,
                            System.currentTimeMillis() - startTime
                        );
                    }
                } else {
                    // 실제 실패 (비즈니스 로직 실패)
                    if (auditLogAnnotation.logOnFailure()) {
                        // ResponseEntity에서 HTTP 상태 코드 추출 또는 ApiResponse 코드 사용
                        String errorCode = extractErrorCodeFromResult(result, apiResponse);
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "요청 처리 실패";
                        String errorMessage = auditLogService.createErrorMessageJson(errorCode, errorMsg);
                        AuditResultStatus resultStatus = determineResultStatusFromCode(errorCode);
                        
                        logAuditEvent(
                            request,
                            joinPoint.getArgs(),
                            auditLogAnnotation,
                            resultStatus,
                            errorMessage,
                            System.currentTimeMillis() - startTime
                        );
                    }
                }
            } else {
                // ApiResponse가 아닌 경우 성공으로 처리
                if (auditLogAnnotation.logOnSuccess()) {
                    logAuditEvent(
                        request,
                        joinPoint.getArgs(),
                        auditLogAnnotation,
                        AuditResultStatus.SUCCESS,
                        null,
                        System.currentTimeMillis() - startTime
                    );
                }
            }
            
        } catch (Throwable e) {
            // 실패 시 로그 기록 (logOnFailure 옵션 확인)
            if (auditLogAnnotation.logOnFailure()) {
                String errorMessage = auditLogService.createStandardErrorMessage("500");
                AuditResultStatus resultStatus = determineErrorStatus(e);
                
                logAuditEvent(
                    request,
                    joinPoint.getArgs(),
                    auditLogAnnotation,
                    resultStatus,
                    errorMessage,
                    System.currentTimeMillis() - startTime
                );
            }
            
            log.error("메서드 실행 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
        
        return result;
    }
    
    /**
     * 현재 HTTP 요청 정보 추출 (안전 처리)
     */
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            // 웹 컨텍스트 외부에서 실행된 경우
            return null;
        }
    }
    
    /**
     * 감사 로그 기록
     */
    private void logAuditEvent(
            HttpServletRequest request,
            Object[] args,
            AuditLog auditLogAnnotation,
            AuditResultStatus resultStatus,
            String errorMessage,
            long processingTime) {
        
        try {
            // 파라미터 포함 여부 확인
            Object[] actualArgs = auditLogAnnotation.includeParameters() ? args : null;
            
            // 감사 로그 생성 (서비스 계층에 위임)
            AuditLogVO auditLog = auditLogService.createAuditLog(
                request,
                actualArgs,
                auditLogAnnotation.actionType(),
                auditLogAnnotation.targetResource(),
                auditLogAnnotation.description(),
                resultStatus,
                errorMessage,
                processingTime,
                auditLogAnnotation.sensitiveFields()
            );
            
            // 비동기로 저장 (서비스 계층에 위임)
            auditLogService.saveAuditLogAsync(auditLog);
            
        } catch (Exception ex) {
            log.error("감사 로그 기록 중 오류 발생: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * 예외 타입에 따른 상세 결과 상태 결정
     */
    private AuditResultStatus determineErrorStatus(Throwable throwable) {
        String exceptionName = throwable.getClass().getSimpleName().toLowerCase();
        
        if (exceptionName.contains("unauthorized") || 
            exceptionName.contains("authentication") ||
            exceptionName.contains("badcredentials")) {
            return AuditResultStatus.UNAUTHORIZED;
        }
        
        if (exceptionName.contains("forbidden") || 
            exceptionName.contains("accessdenied")) {
            return AuditResultStatus.UNAUTHORIZED;
        }
        
        if (exceptionName.contains("validation") || 
            exceptionName.contains("illegalargument")) {
            return AuditResultStatus.FAILURE;
        }
        
        return AuditResultStatus.FAILURE;
    }
    
    /**
     * 응답 객체에서 ApiResponse 추출
     */
    private ApiResponse<?> extractApiResponse(Object result) {
        if (result instanceof ApiResponse) {
            return (ApiResponse<?>) result;
        }
        
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            Object body = responseEntity.getBody();
            if (body instanceof ApiResponse) {
                return (ApiResponse<?>) body;
            }
        }
        
        return null;
    }
    
    /**
     * 결과 객체에서 에러 코드 추출 (ResponseEntity의 HttpStatus 우선, 없으면 ApiResponse 코드)
     */
    private String extractErrorCodeFromResult(Object result, ApiResponse<?> apiResponse) {
        // 1. ResponseEntity의 HttpStatus 코드 확인
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            int statusCode = responseEntity.getStatusCode().value();
            return String.valueOf(statusCode);
        }
        
        // 2. ApiResponse의 code 필드 확인
        if (apiResponse.getCode() != null) {
            return apiResponse.getCode();
        }
        
        // 3. 기본값
        return "400";
    }
    
    /**
     * 에러 코드 기반으로 결과 상태 결정 (HTTP 상태 코드 우선)
     */
    private AuditResultStatus determineResultStatusFromCode(String code) {
        if (code == null) {
            return AuditResultStatus.FAILURE;
        }
        
        // HTTP 상태 코드 우선 분류 (실제 프로젝트에서 사용되는 코드들)
        switch (code) {
            case "401":  // UNAUTHORIZED - 인증 실패, 로그인 필요
            case "403":  // FORBIDDEN - 접근 거부, 권한 없음
                return AuditResultStatus.UNAUTHORIZED;
                
            case "404":  // NOT_FOUND - 페이지/리소스 없음
                return AuditResultStatus.NOT_FOUND;
                
            case "400":  // BAD_REQUEST - 유효성 검사 실패, 파라미터 오류
            case "405":  // METHOD_NOT_ALLOWED - 지원하지 않는 HTTP 메서드
            case "429":  // TOO_MANY_REQUESTS - Rate Limit 초과
                return AuditResultStatus.FAILURE;
                
            case "500":  // INTERNAL_SERVER_ERROR - 시스템 오류
                return AuditResultStatus.FAILURE;
                
            default:
                break;
        }
        
        // HTTP 상태 코드 범위별 분류
        if (code.startsWith("4")) {
            return AuditResultStatus.FAILURE; // 4xx: 클라이언트 오류
        }
        
        if (code.startsWith("5")) {
            return AuditResultStatus.FAILURE; // 5xx: 서버 오류
        }
        
        // 커스텀 코드 분류 (기존 로직 유지)
        if (code.startsWith("1")) {
            // 1000번대: 인증/보안 실패
            if (code.equals("1001") || code.equals("1014")) {
                return AuditResultStatus.UNAUTHORIZED;
            }
            if (code.equals("1011") || code.equals("1012") || code.equals("1013")) {
                return AuditResultStatus.UNAUTHORIZED;
            }
            return AuditResultStatus.UNAUTHORIZED;
        }
        
        if (code.startsWith("2")) {
            // 2000번대: 권한 관련 실패
            return AuditResultStatus.UNAUTHORIZED;
        }
        
        // 기타는 일반 실패로 처리
        return AuditResultStatus.FAILURE;
    }
    
    /**
     * ApiResponse의 코드와 에러 정보를 기반으로 결과 상태 결정 (하위 호환성)
     */
    private AuditResultStatus determineResultStatusFromApiResponse(ApiResponse<?> apiResponse) {
        String code = apiResponse.getCode();
        return determineResultStatusFromCode(code);
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