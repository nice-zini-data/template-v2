package com.zinidata.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.enums.AuditResultStatus;
import com.zinidata.audit.mapper.AuditLogMapper;
import com.zinidata.audit.vo.AuditLogVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 감사 로그 서비스
 * 
 * <p>감사 로그의 생성, 저장, 조회를 담당하는 서비스입니다.</p>
 * 
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>HTTP 요청 정보로부터 감사 로그 생성</li>
 *   <li>민감정보 자동 마스킹 처리</li>
 *   <li>비동기 로그 저장으로 성능 최적화</li>
 *   <li>사용자 인증 정보 자동 추출</li>
 *   <li>다양한 파라미터 소스 통합 수집</li>
 * </ul>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * application.yml에서 app.code 값을 주입받아 프로젝트 타입으로 사용
     */
    @Value("${app.code:NBZM}")
    private String appCode;
    
    /**
     * 감사 로그 비동기 저장
     * 
     * <p>성능 영향을 최소화하기 위해 비동기로 처리됩니다.</p>
     * 
     * @param auditLogVO 저장할 감사 로그
     */
    @Async("auditLogExecutor")
    public void saveAuditLogAsync(AuditLogVO auditLogVO) {
        try {
            auditLogMapper.insertAuditLog(auditLogVO);
            log.debug("감사 로그 비동기 저장 완료: {} - {}", auditLogVO.getActionType(), auditLogVO.getTargetResource());
        } catch (Exception e) {
            log.error("감사 로그 비동기 저장 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 감사 로그 동기 저장
     * 
     * <p>중요한 보안 이벤트 등에서 확실한 저장이 필요한 경우 사용합니다.</p>
     * 
     * @param auditLogVO 저장할 감사 로그
     */
    public void saveAuditLogSync(AuditLogVO auditLogVO) {
        try {
            auditLogMapper.insertAuditLog(auditLogVO);
            log.debug("감사 로그 동기 저장 완료: {} - {}", auditLogVO.getActionType(), auditLogVO.getTargetResource());
        } catch (Exception e) {
            log.error("감사 로그 동기 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("감사 로그 저장 실패", e);
        }
    }
    
    /**
     * HTTP 요청 정보로부터 감사 로그 생성
     * 
     * @param request HTTP 요청
     * @param args 메서드 인자
     * @param actionType 액션 타입
     * @param targetResource 대상 리소스
     * @param description 추가 설명
     * @param resultStatus 결과 상태
     * @param errorMessage 에러 메시지 (선택적)
     * @param processingTime 처리 시간 (밀리초)
     * @param sensitiveFields 민감정보 필드 배열
     * @return 생성된 감사 로그
     */
    public AuditLogVO createAuditLog(
            HttpServletRequest request,
            Object[] args,
            AuditActionType actionType,
            String targetResource,
            String description,
            AuditResultStatus resultStatus,
            String errorMessage,
            Long processingTime,
            String[] sensitiveFields) {
        
        AuditLogVO auditLog = new AuditLogVO();
        
        // 기본 정보 설정
        auditLog.setMemNo(getCurrentUserMemNo());
        auditLog.setPrjType(appCode);
        auditLog.setClientIp(getClientIpAddress(request));
        
        // request가 null인 경우 (세션 만료 등) 기본값 설정
        if (request != null) {
            auditLog.setRequestUri(decodeUrl(request.getRequestURI()));
            auditLog.setHttpMethod(request.getMethod());
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setReferrer(request.getHeader("Referer"));
        } else {
            auditLog.setRequestUri("system-event");
            auditLog.setHttpMethod("SYSTEM");
            auditLog.setUserAgent("SystemProcess");
            auditLog.setReferrer(null);
        }
        auditLog.setActionType(actionType);
        auditLog.setTargetResource(decodeUrl(targetResource));
        auditLog.setResultStatus(resultStatus);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setAccessTime(LocalDateTime.now());
        auditLog.setProcessingTime(processingTime);
        
        // 세션 ID 추가 (비인증 사용자도 세션 생성하여 추적)
        if (request != null) {
            HttpSession session = request.getSession(true); // 세션이 없으면 생성
            auditLog.setSessionId(session.getId());
        }
        
        // 파라미터 수집 및 마스킹 (request가 null인 경우 빈 JSON)
        String maskedParameters = (request != null) ? 
            collectAndMaskParameters(request, args, sensitiveFields) : "{}";
        auditLog.setParameters(maskedParameters);
        
        return auditLog;
    }
    
    /**
     * 에러메시지를 JSON 포맷으로 생성
     */
    public String createErrorMessageJson(String errorCode, String errorMessage) {
        try {
            Map<String, String> errorMap = new LinkedHashMap<>(); // 순서 보장을 위해 LinkedHashMap 사용
            errorMap.put("error_code", errorCode);
            errorMap.put("error_message", errorMessage);
            return objectMapper.writeValueAsString(errorMap);
        } catch (JsonProcessingException e) {
            log.warn("에러메시지 JSON 변환 실패: {}", e.getMessage());
            return String.format("{\"error_code\":\"%s\",\"error_message\":\"%s\"}", errorCode, errorMessage);
        }
    }
    
    /**
     * 표준 에러코드에 따른 에러메시지 생성
     */
    public String createStandardErrorMessage(String errorCode) {
        String errorMessage;
        switch (errorCode) {
            case "401":
                errorMessage = "인증 실패";
                break;
            case "404":
                errorMessage = "페이지를 찾을 수 없습니다";
                break;
            case "500":
                errorMessage = "서버 오류가 발생했습니다";
                break;
            default:
                errorMessage = "알 수 없는 오류";
                break;
        }
        return createErrorMessageJson(errorCode, errorMessage);
    }
    
    /**
     * 현재 로그인된 사용자의 회원 번호 추출
     * 
     * @return 회원 번호 (로그인되지 않은 경우 null)
     */
    private Long getCurrentUserMemNo() {
        try {
            // 1. Spring Security Authentication에서 사용자 정보 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                // 2. HTTP 요청에서 세션 정보 가져오기
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    HttpSession session = request.getSession(false);
                    
                    if (session != null) {
                        Long memNo = (Long) session.getAttribute("memNo");
                        if (memNo != null) {
                            log.debug("세션에서 회원번호 추출: {}", memNo);
                            return memNo;
                        }
                    }
                }
                
                log.debug("Spring Security 인증됨 but 세션에서 회원번호 없음: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.debug("현재 사용자 정보 추출 실패: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     * 
     * @param request HTTP 요청 (null 가능)
     * @return 클라이언트 IP 주소
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // request가 null인 경우 시스템 이벤트로 처리
        if (request == null) {
            return "system";
        }
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "X-Original-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 여러 IP가 있는 경우 첫 번째 사용
                int commaIndex = ip.indexOf(',');
                if (commaIndex != -1) {
                    ip = ip.substring(0, commaIndex).trim();
                }
                
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }
        
        // 헤더에서 찾지 못한 경우 기본 remote address 사용
        String remoteAddr = request.getRemoteAddr();
        return isValidIp(remoteAddr) ? remoteAddr : "UNKNOWN";
    }
    
    /**
     * IP 주소 유효성 검증
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip) || 
            "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return false;
        }
        
        // IPv4 간단 검증
        if (ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            return true;
        }
        
        // IPv6 간단 검증 (완벽하지 않지만 기본적인 형식 체크)
        if (ip.contains(":") && ip.length() > 2) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 요청 파라미터 수집 및 민감정보 마스킹
     * 
     * @param request HTTP 요청
     * @param args 메서드 인자
     * @param sensitiveFields 마스킹할 필드들
     * @return JSON 형태의 마스킹된 파라미터 문자열
     */
    private String collectAndMaskParameters(HttpServletRequest request, Object[] args, String[] sensitiveFields) {
        Map<String, Object> allParams = new HashMap<>();
        
        try {
            // 1. Query Parameter 수집
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                
                if (isSensitiveField(paramName, sensitiveFields)) {
                    allParams.put(paramName, maskSensitiveValue(paramValues));
                } else {
                    allParams.put(paramName, paramValues.length == 1 ? paramValues[0] : paramValues);
                }
            }
            
            // 2. Method Arguments 수집 (VO 객체 및 Map 처리, Spring 내부 객체 제외)
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg != null && !isSpringInternalObject(arg)) {
                        if (arg instanceof Map) {
                            // Map 객체인 경우 (@RequestBody Map<String, Object> 등)
                            extractMapFields(allParams, (Map<String, Object>) arg, sensitiveFields);
                        } else if (isVoObject(arg)) {
                            // VO 객체인 경우
                            extractVoFields(allParams, arg, sensitiveFields);
                        }
                    }
                }
            }
            
            return objectMapper.writeValueAsString(allParams);
            
        } catch (JsonProcessingException e) {
            log.warn("파라미터 JSON 변환 실패: {}", e.getMessage());
            return "{\"error\":\"파라미터 수집 실패\"}";
        } catch (Exception e) {
            log.warn("파라미터 수집 중 오류: {}", e.getMessage());
            return "{\"error\":\"파라미터 수집 중 오류 발생\"}";
        }
    }
    
    /**
     * Spring 내부 객체인지 확인 (감사로그에서 제외할 객체들)
     */
    private boolean isSpringInternalObject(Object obj) {
        String className = obj.getClass().getName();
        return className.startsWith("org.springframework") ||
               className.startsWith("jakarta.servlet") ||
               className.contains("Model") ||
               className.contains("HttpServletRequest") ||
               className.contains("HttpServletResponse") ||
               className.contains("HttpSession") ||
               className.contains("BindingResult") ||
               className.contains("Errors") ||
               className.contains("RedirectAttributes");
    }

    /**
     * VO 객체인지 확인
     */
    private boolean isVoObject(Object obj) {
        String className = obj.getClass().getSimpleName().toLowerCase();
        return className.endsWith("vo") || 
               className.endsWith("dto") || 
               className.endsWith("request") || 
               className.endsWith("form");
    }
    
    /**
     * Map 객체에서 필드 추출 (@RequestBody Map<String, Object> 등)
     */
    private void extractMapFields(Map<String, Object> params, Map<String, Object> map, String[] sensitiveFields) {
        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();
                
                if (fieldValue != null) {
                    if (isSensitiveField(fieldName, sensitiveFields)) {
                        params.put(fieldName, maskSensitiveField(fieldName, fieldValue.toString()));
                    } else {
                        params.put(fieldName, fieldValue);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Map 필드 추출 실패: {}", e.getMessage());
        }
    }

    /**
     * VO 객체에서 필드 추출 (리플렉션 사용)
     */
    private void extractVoFields(Map<String, Object> params, Object vo, String[] sensitiveFields) {
        try {
            java.lang.reflect.Field[] fields = vo.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(vo);
                
                if (fieldValue != null) {
                    if (isSensitiveField(fieldName, sensitiveFields)) {
                        params.put(fieldName, maskSensitiveField(fieldName, fieldValue.toString()));
                    } else {
                        params.put(fieldName, fieldValue);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("VO 필드 추출 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 민감정보 필드인지 확인
     */
    private boolean isSensitiveField(String fieldName, String[] sensitiveFields) {
        if (fieldName == null || sensitiveFields == null) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        
        for (String sensitiveField : sensitiveFields) {
            if (lowerFieldName.contains(sensitiveField.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 민감정보 배열 마스킹
     */
    private Object maskSensitiveValue(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        
        if (values.length == 1) {
            return maskString(values[0]);
        }
        
        String[] maskedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            maskedValues[i] = maskString(values[i]);
        }
        
        return maskedValues;
    }
    
    /**
     * 필드 타입에 따른 민감정보 마스킹
     */
    private String maskSensitiveField(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        
        // 이메일 마스킹
        if (lowerFieldName.contains("email")) {
            return maskEmail(value);
        }
        
        // 전화번호 마스킹
        if (lowerFieldName.contains("phone") || lowerFieldName.contains("mobile") || lowerFieldName.contains("tel")) {
            return maskPhoneNumber(value);
        }
        
        // 비밀번호, 토큰 등은 완전 마스킹
        if (lowerFieldName.contains("password") || lowerFieldName.contains("pwd") || 
            lowerFieldName.contains("token") || lowerFieldName.contains("secret") || 
            lowerFieldName.contains("key")) {
            return "****";
        }
        
        // 기본 문자열 마스킹
        return maskString(value);
    }
    
    /**
     * 이메일 마스킹 (앞 1-2자리 + @ 도메인은 그대로)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return maskString(email);
        }
        
        try {
            String[] parts = email.split("@");
            String localPart = parts[0];
            String domain = parts[1];
            
            if (localPart.length() <= 2) {
                return localPart.charAt(0) + "*@" + domain;
            } else {
                return localPart.substring(0, 2) + "***@" + domain;
            }
        } catch (Exception e) {
            return maskString(email);
        }
    }
    
    /**
     * 전화번호 마스킹 (010-1234-****) 
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // 숫자만 추출
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        
        if (digitsOnly.length() < 8) {
            return maskString(phoneNumber);
        }
        
        try {
            if (digitsOnly.length() == 11 && digitsOnly.startsWith("010")) {
                // 010-1234-5678 -> 010-1234-****
                return digitsOnly.substring(0, 3) + "-" + 
                       digitsOnly.substring(3, 7) + "-****";
            } else if (digitsOnly.length() == 10) {
                // 02-1234-5678 -> 02-1234-****
                return digitsOnly.substring(0, 2) + "-" + 
                       digitsOnly.substring(2, 6) + "-****";
            } else {
                // 기타 형태는 뒤 4자리만 마스킹
                int maskStart = Math.max(0, digitsOnly.length() - 4);
                return digitsOnly.substring(0, maskStart) + "****";
            }
        } catch (Exception e) {
            return maskString(phoneNumber);
        }
    }
    
    /**
     * 기본 문자열 마스킹 (앞 1-2자리 제외하고 *)
     */
    private String maskString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        if (value.length() <= 2) {
            return "*".repeat(value.length());
        } else if (value.length() <= 4) {
            return value.charAt(0) + "*".repeat(value.length() - 1);
        } else {
            return value.substring(0, 2) + "*".repeat(value.length() - 2);
        }
    }
    
    /**
     * URL 디코딩 처리
     * 
     * @param url 디코딩할 URL 문자열
     * @return 디코딩된 URL 문자열 (실패 시 원본 반환)
     */
    private String decodeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 디코딩 실패 시 원본 URL 반환
            log.debug("URL 디코딩 실패, 원본 반환: {}", url, e);
            return url;
        }
    }
} 