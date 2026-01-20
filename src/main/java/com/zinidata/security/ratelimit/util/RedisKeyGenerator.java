package com.zinidata.security.ratelimit.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis 키 생성 유틸리티
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Component
public class RedisKeyGenerator {
    
    @Value("${app.code:NBZM}")
    private String appCode;
    
    private static final String SEPARATOR = ":";
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
    
    /**
     * 문자인증 발송 제한 키 생성
     * 
     * @param mobileNo 휴대폰 번호
     * @return Redis 키 (예: NBZM:cert:limit:01012345678:2025072114)
     */
    public String generateCertLimitKey(String mobileNo) {
        String currentHour = LocalDateTime.now().format(HOUR_FORMATTER);
        return String.join(SEPARATOR, 
            appCode, "cert", "limit", mobileNo, currentHour);
    }
    
    /**
     * API 호출 제한 키 생성
     * 
     * @param clientIp 클라이언트 IP
     * @param endpoint API 엔드포인트 (예: /api/report/aaa)
     * @return Redis 키 (예: NBZM:api:limit:192.168.1.100:api-report-aaa:2025072114)
     */
    public String generateApiLimitKey(String clientIp, String endpoint) {
        String currentHour = LocalDateTime.now().format(HOUR_FORMATTER);
        String sanitizedEndpoint = sanitizeEndpoint(endpoint);
        return String.join(SEPARATOR, 
            appCode, "api", "limit", clientIp, sanitizedEndpoint, currentHour);
    }
    
    /**
     * IP 차단 키 생성
     * 
     * @param ip IP 주소
     * @return Redis 키 (예: NBZM:ip:blocked:192.168.1.100)
     */
    public String generateIpBlockKey(String ip) {
        return String.join(SEPARATOR, 
            appCode, "ip", "blocked", ip);
    }
    
    /**
     * 엔드포인트를 Redis 키에 사용 가능한 형태로 변환
     * 
     * @param endpoint API 엔드포인트
     * @return 변환된 엔드포인트 (예: /api/report/aaa -> api-report-aaa)
     */
    private String sanitizeEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return "unknown";
        }
        
        return endpoint
            .replaceAll("^/", "")           // 시작 슬래시 제거
            .replaceAll("/", "-")           // 슬래시를 하이픈으로 변경
            .replaceAll("[^a-zA-Z0-9\\-]", ""); // 영숫자와 하이픈만 허용
    }
    
    /**
     * 현재 시간 기준 시간 키 생성
     * 
     * @return 시간 키 (예: 2025072114)
     */
    public String getCurrentHourKey() {
        return LocalDateTime.now().format(HOUR_FORMATTER);
    }
    
    /**
     * 프로젝트 코드 반환
     * 
     * @return 프로젝트 코드
     */
    public String getProjectCode() {
        return appCode;
    }
} 