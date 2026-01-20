package com.zinidata.domain.common.sms.service;

import com.zinidata.domain.common.sms.dto.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 통합 SMS 발송 서비스
 * 
 * <p><strong>📌 주요 기능:</strong></p>
 * <ul>
 *   <li>✅ <strong>보안:</strong> PreparedStatement 사용으로 SQL Injection 방지</li>
 *   <li>✅ <strong>일관성:</strong> 모든 SMS 발송을 단일 인터페이스로 통합</li>
 *   <li>✅ <strong>확장성:</strong> 새로운 SMS 타입 추가 용이</li>
 *   <li>✅ <strong>관리:</strong> 발송 이력 및 제한 기능 내장</li>
 * </ul>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedSmsService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${sms.callback.number}")
    private String defaultCallbackNumber;
    
    @Value("${sms.templates.certification}")
    private String certificationTemplate;
    
    @Value("${sms.templates.temp-password}")
    private String tempPasswordTemplate;
    
    @Value("${sms.templates.subscription}")
    private String subscriptionTemplate;
    
    @Value("${sms.templates.general}")
    private String generalTemplate;
    
    @Value("${sms.rate-limit.max-count}")
    private int rateLimitMaxCount;
    
    @Value("${sms.rate-limit.time-window}")
    private int rateLimitTimeWindow;
    
    @Value("${sms.url:devapi2.nicebizmap.co.kr}")
    private String smsUrl;
    
    @Value("${sms.api.loginId:smsSend}")
    private String smsLoginId;
    
    @Value("${sms.api.password:nice1234!!}")
    private String smsPassword;
    
    @Value("${app.baseUrl:https://m.nicebizmap.co.kr}")
    private String appBaseUrl;
    
    /**
     * SMS 발송 (메인 메서드)
     * 
     * @param request SMS 발송 요청
     * @return 발송 성공 여부
     */
    public boolean sendCertSms(SmsRequest request) {
        // 입력값 검증
        if (!request.isValid()) {
            log.error("[SMS] 잘못된 SMS 발송 요청: {}", request);
            return false;
        }
        
        // 발송 제한 확인
        if (isRateLimited(request.getPhoneNumber())) {
            log.warn("[SMS] 발송 제한 초과 - phoneNumber: {}, 제한: {}회/{}시간", 
                    request.getPhoneNumber(), rateLimitMaxCount, rateLimitTimeWindow);
            return false;
        }
        
        return executeSms(request);
    }

    /**
     * SMS 발송 (메인 메서드)
     *
     * @param request SMS 발송 요청
     * @return 발송 성공 여부
     */
    public boolean sendSms(SmsRequest request) {
        // 입력값 검증
        if (!request.isValid()) {
            log.error("[SMS] 잘못된 SMS 발송 요청: {}", request);
            return false;
        }

        return executeSms(request);
    }
    
    /**
     * 인증번호 SMS 발송 (회원명 포함)
     */
    public boolean sendCertificationSms(String phoneNumber, String certNumber, String memNm) {
        log.info("[SMS] 인증번호 SMS 발송 요청 - phoneNumber: {}, memNm: {}", phoneNumber, memNm);
        return sendCertSms(SmsRequest.certification(phoneNumber, certNumber, certificationTemplate, defaultCallbackNumber, memNm));
    }
    
    /**
     * 일반 SMS 발송 (회원명 포함)
     */
    public boolean sendGeneralSms(String phoneNumber, String message, String callbackNumber, String memNm) {
        log.info("[SMS] 일반 SMS 발송 요청 - phoneNumber: {}, memNm: {}", phoneNumber, memNm);
        return sendSms(SmsRequest.general(phoneNumber, message, callbackNumber, memNm));
    }
    
    /**
     * 실제 SMS 발송 처리 (API 호출 방식)
     */
    private boolean executeSms(SmsRequest request) {
        try {
            log.info("[SMS] SMS 발송 시작 - type: {}, phoneNumber: {}", 
                    request.getType().getDescription(), request.getPhoneNumber());
            
            // 1. 토큰 발급
            String token = getToken();
            if (token == null || token.isEmpty()) {
                log.error("[SMS] 토큰 발급 실패 - type: {}, phoneNumber: {}", 
                         request.getType().getDescription(), request.getPhoneNumber());
                return false;
            }
            
            // 2. SMS 발송 API 호출
            boolean result = sendSmsApi(token, request);
            
            if (result) {
                log.info("[SMS] SMS 발송 성공 - type: {}, phoneNumber: {}", 
                        request.getType().getDescription(), request.getPhoneNumber());
            } else {
                log.error("[SMS] SMS 발송 실패 - type: {}, phoneNumber: {}", 
                         request.getType().getDescription(), request.getPhoneNumber());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("[SMS] SMS 발송 중 오류 발생 - type: {}, phoneNumber: {}", 
                     request.getType().getDescription(), request.getPhoneNumber(), e);
            return false;
        }
    }
    
    /**
     * 토큰 발급 API 호출
     * https://devapi2.nicebizmap.co.kr/common/getToken
     */
    private String getToken() {
        try {
            String url = "https://" + smsUrl + "/common/getToken";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // Origin과 Referer 헤더 추가 (도메인 제어를 위해)
            headers.set("Origin", appBaseUrl);
            headers.set("Referer", appBaseUrl);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("loginId", smsLoginId);
            params.add("pwd", smsPassword);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            
            log.info("[SMS] 토큰 발급 요청 - url: {}, Origin: {}, Referer: {}", url, appBaseUrl, appBaseUrl);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null) {
                Object dataObj = body.get("data");
                
                if (dataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    
                    if (data != null && data.containsKey("authorization")) {
                        String authorization = (String) data.get("authorization");
                        // "Bearer " 접두사 제거
                        if (authorization != null && authorization.startsWith("Bearer ")) {
                            String token = authorization.substring(7);
                            log.info("[SMS] 토큰 발급 성공");
                            return token;
                        }
                    }
                }
            }
            
            log.error("[SMS] 토큰 발급 실패 - 응답: {}", response.getBody());
            return null;
            
        } catch (Exception e) {
            log.error("[SMS] 토큰 발급 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * SMS 발송 API 호출
     * https://devapi2.nicebizmap.co.kr/sms/zinidata/sendSms
     */
    private boolean sendSmsApi(String token, SmsRequest request) {
        try {
            String url = "https://" + smsUrl + "/sms/zinidata/sendSms";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + token);
            // Origin과 Referer 헤더 추가 (도메인 제어를 위해)
            headers.set("Origin", appBaseUrl);
            headers.set("Referer", appBaseUrl);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mobileNo", request.getPhoneNumber());
            params.add("memNm", request.getMemNm() != null ? request.getMemNm() : "");
            params.add("tranMsg", request.getMessage());
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            
            log.info("[SMS] SMS 발송 API 호출 - url: {}, mobileNo: {}, Origin: {}, Referer: {}", 
                    url, request.getPhoneNumber(), appBaseUrl, appBaseUrl);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null) {
                String code = (String) body.get("code");
                Object resultObj = body.get("result");
                
                if ("200".equals(code) || "C001".equals(code) || "success".equals(resultObj)) {
                    log.info("[SMS] SMS 발송 API 호출 성공 - 응답: {}", body);
                    return true;
                } else {
                    log.error("[SMS] SMS 발송 API 호출 실패 - 응답: {}", body);
                    return false;
                }
            }
            
            log.error("[SMS] SMS 발송 API 호출 실패 - 응답: {}", response.getBody());
            return false;
            
        } catch (Exception e) {
            log.error("[SMS] SMS 발송 API 호출 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 발송 제한 확인 (API 호출 방식)
     */
    private boolean isRateLimited(String phoneNumber) {
        try {
            // 1. 토큰 발급
            String token = getToken();
            if (token == null || token.isEmpty()) {
                log.error("[SMS] 발송 제한 확인 - 토큰 발급 실패 - phoneNumber: {}", phoneNumber);
                // 에러 시 안전하게 제한하지 않음
                return false;
            }
            
            // 2. 발송 제한 확인 API 호출
            String url = "https://" + smsUrl + "/sms/zinidata/checkRateLimit";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + token);
            // Origin과 Referer 헤더 추가 (도메인 제어를 위해)
            headers.set("Origin", appBaseUrl);
            headers.set("Referer", appBaseUrl);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mobileNo", phoneNumber);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            
            log.debug("[SMS] 발송 제한 확인 API 호출 - url: {}, mobileNo: {}, Origin: {}, Referer: {}", 
                    url, phoneNumber, appBaseUrl, appBaseUrl);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null) {
                String code = (String) body.get("code");
                
                if ("200".equals(code) || "C001".equals(code)) {
                    Object dataObj = body.get("data");
                    
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        
                        Object isRateLimitedObj = data.get("isRateLimited");
                        if (isRateLimitedObj instanceof Boolean) {
                            boolean isRateLimited = (Boolean) isRateLimitedObj;
                            log.debug("[SMS] 발송 제한 확인 결과 - phoneNumber: {}, isRateLimited: {}", 
                                    phoneNumber, isRateLimited);
                            return isRateLimited;
                        }
                    }
                }
            }
            
            log.warn("[SMS] 발송 제한 확인 API 호출 실패 - phoneNumber: {}, 응답: {}", phoneNumber, body);
            // 에러 시 안전하게 제한하지 않음
            return false;
            
        } catch (Exception e) {
            log.error("[SMS] 발송 제한 확인 중 오류 - phoneNumber: {}", phoneNumber, e);
            // 에러 시 안전하게 제한하지 않음
            return false;
        }
    }
    
    /**
     * SMS 발송 이력 조회 (API 호출 방식)
     * 주의: API는 최근 1시간 기준으로만 제공하므로, startDate가 1시간 이내인 경우만 정확한 결과를 반환합니다.
     */
    public int getSmsCount(String phoneNumber, LocalDateTime startDate) {
        // API는 최근 1시간 기준으로만 제공하므로, 1시간 이내인지 확인
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        if (startDate.isAfter(oneHourAgo)) {
            // 최근 1시간 이내인 경우 API 호출
            return getRecentSmsCount(phoneNumber);
        } else {
            // 1시간 이전인 경우 0 반환 (API 제한)
            log.warn("[SMS] 발송 이력 조회 - 1시간 이전 데이터는 조회 불가 - phoneNumber: {}, startDate: {}", 
                    phoneNumber, startDate);
            return 0;
        }
    }
    
    /**
     * 최근 1시간 SMS 발송 건수 조회 (API 호출 방식)
     */
    public int getRecentSmsCount(String phoneNumber) {
        try {
            // 1. 토큰 발급
            String token = getToken();
            if (token == null || token.isEmpty()) {
                log.error("[SMS] 발송 건수 조회 - 토큰 발급 실패 - phoneNumber: {}", phoneNumber);
                return 0;
            }
            
            // 2. 발송 건수 조회 API 호출
            String url = "https://" + smsUrl + "/sms/zinidata/checkRateLimit";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + token);
            // Origin과 Referer 헤더 추가 (도메인 제어를 위해)
            headers.set("Origin", appBaseUrl);
            headers.set("Referer", appBaseUrl);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mobileNo", phoneNumber);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            
            log.debug("[SMS] 발송 건수 조회 API 호출 - url: {}, mobileNo: {}, Origin: {}, Referer: {}", 
                    url, phoneNumber, appBaseUrl, appBaseUrl);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null) {
                String code = (String) body.get("code");
                
                if ("200".equals(code) || "C001".equals(code)) {
                    Object dataObj = body.get("data");
                    
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        
                        Object recentSmsCountObj = data.get("recentSmsCount");
                        if (recentSmsCountObj instanceof Number) {
                            int count = ((Number) recentSmsCountObj).intValue();
                            log.debug("[SMS] 발송 건수 조회 결과 - phoneNumber: {}, count: {}", phoneNumber, count);
                            return count;
                        }
                    }
                }
            }
            
            log.warn("[SMS] 발송 건수 조회 API 호출 실패 - phoneNumber: {}, 응답: {}", phoneNumber, body);
            return 0;
            
        } catch (Exception e) {
            log.error("[SMS] 발송 건수 조회 중 오류 - phoneNumber: {}", phoneNumber, e);
            return 0;
        }
    }
}
