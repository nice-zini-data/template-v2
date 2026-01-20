package com.zinidata.domain.common.cert.controller.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.common.cert.service.CertService;
import com.zinidata.security.ratelimit.exception.RateLimitExceededException;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 문자인증 API 컨트롤러
 * 
 * <p><strong>주요 특징:</strong></p>
 * <ul>
 *   <li>새로운 Status enum 기반 응답 처리</li>
 *   <li>예외 타입별 Status 매핑 (메시지 내용 기반 분기 제거)</li>
 *   <li>휴대폰 인증번호 발송 및 검증</li>
 *   <li>보안 강화: 발송 횟수 제한, 시간 제한</li>
 *   <li>민감 정보 보호: 휴대폰 번호 마스킹</li>
 * </ul>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[cert] 문자인증", description = "휴대폰 문자 인증 API - 인증번호 발송, 검증, 보안 제한")
@RestController
@RequestMapping("/api/cert")
@RequiredArgsConstructor
@Slf4j
public class CertApiController {

    private final CertService certService;

    /**
     * 인증번호 발송
     */
    @ApiOperation(value = "인증번호 발송 API", notes = "휴대폰 SMS 인증번호 발송")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/cert/send", sensitiveFields = {"mobileNo"})
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendCertNumber(
            @RequestBody Map<String, Object> certRequest,
            HttpServletRequest request) {

        String mobileNo = (String) certRequest.get("mobileNo");
        String pathName = (String) certRequest.get("pathName");
        String memNm = (String) certRequest.get("memNm");
        
        log.info("인증번호 발송 요청 - mobileNo: {}", mobileNo);

        try {
            // 입력값 검증
            validateSendRequest(mobileNo, pathName);
            
            // 인증번호 발송
            Map<String, Object> result = certService.sendCertNumber(mobileNo, pathName, request, memNm);
            
            // 성공 응답
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(result);
            return ResponseEntity.ok(apiResponse);

        } catch (ValidationException e) {
            // 유효성 검사 실패
            log.warn("인증번호 발송 유효성 검사 실패 - mobileNo: {}, reason: {}", mobileNo, e.getMessage());
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (RateLimitExceededException e) {
            // Rate Limit 초과
            log.warn("인증번호 발송 Rate Limit 초과 - mobileNo: {}, reason: {}", mobileNo, e.getMessage());
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.error("인증번호 발송 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(apiResponse);

        } catch (Exception e) {
            // 기타 오류
            log.error("인증번호 발송 중 오류 발생 - mobileNo: {}", mobileNo, e);
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.error("인증번호 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * 인증번호 확인
     */
    @ApiOperation(value = "인증번호 확인 API", notes = "휴대폰 SMS 인증번호 확인")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/cert/verify", sensitiveFields = {"mobileNo", "certNo"})
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyCertNumber(
            @RequestBody Map<String, Object> verifyRequest,
            HttpServletRequest request) {

        String mobileNo = (String) verifyRequest.get("mobileNo");
        String certNo = (String) verifyRequest.get("certNo");

        log.info("인증번호 확인 요청 - mobileNo: {}, certNo: {}", mobileNo, certNo);

        try {
            // 입력값 검증
            validateVerifyRequest(mobileNo, certNo);
            
            // 인증번호 확인
            Map<String, Object> result = certService.verifyCertNumber(mobileNo, certNo, request);
            
            // 성공 응답
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(result);
            return ResponseEntity.ok(apiResponse);

        } catch (ValidationException e) {
            // 유효성 검사 실패
            log.warn("인증번호 확인 유효성 검사 실패 - mobileNo: {}, reason: {}", mobileNo, e.getMessage());
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception e) {
            // 기타 오류
            log.error("인증번호 확인 중 오류 발생 - mobileNo: {}", mobileNo, e);
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.error("인증번호 확인 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * 인증번호 발송 요청 입력값 검증
     */
    private void validateSendRequest(String mobileNo, String pathName) throws ValidationException {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            throw new ValidationException("휴대폰번호를 입력해주세요.");
        }
        
        // 휴대폰번호 형식 검증 (다양한 형식 지원)
        String normalizedMobileNo = normalizePhoneNumber(mobileNo);
        if (normalizedMobileNo == null) {
            throw new ValidationException("올바른 휴대폰번호 형식이 아닙니다. (010-XXXX-XXXX 또는 +82 10-XXXX-XXXX)");
        }
        
        if (pathName == null || pathName.trim().isEmpty()) {
            throw new ValidationException("페이지 경로 정보가 필요합니다.");
        }
    }
    
    /**
     * 휴대폰 번호 정규화 (다양한 형식을 010XXXXXXXX 형식으로 변환)
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        
        // 공백, 하이픈, 괄호 제거
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        try {
            // +82로 시작하는 경우 (국제 형식: +82 10-4252-3713)
            if (cleaned.startsWith("+82")) {
                // +82 10-4252-3713 → +821042523713 → 01042523713
                if (cleaned.length() >= 13 && cleaned.startsWith("+8210")) {
                    return "010" + cleaned.substring(5); // +8210 다음부터 (5번째 인덱스부터)
                }
            }
            // 010으로 시작하는 경우 (이미 정규화됨: 010-4252-3713)
            else if (cleaned.startsWith("010")) {
                if (cleaned.length() == 11) {
                    return cleaned;
                }
            }
            // 82로 시작하는 경우 (국가코드만 있는 경우: 82 10-4252-3713)
            else if (cleaned.startsWith("82")) {
                if (cleaned.length() >= 12 && cleaned.startsWith("8210")) {
                    return "010" + cleaned.substring(3);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 인증번호 확인 요청 입력값 검증
     */
    private void validateVerifyRequest(String mobileNo, String certNo) throws ValidationException {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            throw new ValidationException("휴대폰번호를 입력해주세요.");
        }
        
        // 휴대폰번호 형식 검증 (다양한 형식 지원)
        String normalizedMobileNo = normalizePhoneNumber(mobileNo);
        if (normalizedMobileNo == null) {
            throw new ValidationException("올바른 휴대폰번호 형식이 아닙니다. (010-XXXX-XXXX 또는 +82 10-XXXX-XXXX)");
        }
        
        if (certNo == null || certNo.trim().isEmpty()) {
            throw new ValidationException("인증번호를 입력해주세요.");
        }
        
        // 인증번호 형식 검증 (4-6자리 숫자)
        if (!certNo.trim().matches("^\\d{4,6}$")) {
            throw new ValidationException("올바른 인증번호 형식이 아닙니다.");
        }
    }
}