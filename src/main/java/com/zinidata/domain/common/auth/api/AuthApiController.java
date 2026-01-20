package com.zinidata.domain.common.auth.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.common.auth.service.AuthService;
import com.zinidata.domain.common.auth.vo.MemberVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

/**
 * 통합 인증 API 컨트롤러
 * 
 * <p>사용자 인증, 회원가입, 세션 관리 등 인증 관련 API를 제공합니다.</p>
 * <p>Redis 기반 세션 관리와 Spring Security를 통한 보안 처리를 담당합니다.</p>
 * <p>아이디 존재 여부 확인, 자동 회원가입 기능 포함</p>
 * 
 * @author ZiniData 개발팀
 * @since 2.0
 */
@Tag(name = "[auth] 인증 관리", description = "사용자 인증 관련 API - 로그인, 회원가입, 세션 관리, 중복 검사, 자동 회원가입")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthApiController {

    private final AuthService authService;
    /**
     * 🟢 아이디 존재 여부 확인
     */
    @Operation(summary = "🟢 아이디 존재 여부 확인", description = "로그인 시 아이디 존재 여부 확인")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/check-user")
    @PostMapping("/check-user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkUser(
            @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {

        log.info("[AUTH] 아이디 존재 여부 확인 요청 - loginId: {}", requestVo.getLoginId());

        // 아이디 존재 여부 확인
        boolean exists = authService.checkUserExists(requestVo);
        
        // 응답 생성
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("loginId", requestVo.getLoginId());
        
        String message = exists ? "존재하는 아이디입니다." : "존재하지 않는 아이디입니다.";
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(result, message);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 🟢 회원가입
     */
    @Operation(summary = "🟢 회원가입", description = "신규 회원 가입 처리 (자동 회원가입용)")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/register")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {
        try {
            // 회원가입 처리
            Map<String, Object> result = authService.register(requestVo);

            // 성공 응답
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "회원가입이 완료되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[AUTH] 회원가입 검증 실패: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[AUTH] 회원가입 중 오류 발생", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error("회원가입 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🟢 로그인
     */
    @Operation(summary = "🟢 로그인", description = "사용자 로그인 처리")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
        @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {
        
        log.info("[AUTH] 로그인 요청 - loginId: {}", requestVo.getLoginId());

        try {
            // 로그인 처리
            Map<String, Object> result = authService.login(requestVo, request);

            // 성공 응답
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "로그인되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[AUTH] 로그인 검증 실패: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[AUTH] 로그인 중 오류 발생", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error("로그인 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🟢 아이디 중복 체크
     */
    @Operation(summary = "🟢 아이디 중복 체크", description = "회원가입 시 아이디 중복 여부 확인")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/check-userid")
    @PostMapping("/check-userid")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkUserId(
            @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {

        // 검증 및 처리
        authService.validateAndCheckLoginIdDuplicate(requestVo);
        
        // 성공 응답
        Map<String, Object> result = new HashMap<>();
        result.put("available", true);
        result.put("loginId", requestVo.getLoginId());
        
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(result, "사용 가능한 아이디입니다.");
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 🟢 이메일 중복 체크
     */
    @Operation(summary = "🟢 이메일 중복 체크", description = "회원가입 시 이메일 중복 여부 확인")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/check-email")
    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmail(
        @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {

        String emailAddr = requestVo.getEmailAddr();
        log.info("[AUTH] 이메일 중복 체크 요청 - emailAddr: {}", emailAddr);

        // 검증 및 처리
        authService.validateAndCheckEmailDuplicate(emailAddr);

        // 성공 응답
        Map<String, Object> result = new HashMap<>();
        result.put("available", true);
        result.put("emailAddr", emailAddr.trim());

        ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "사용 가능한 이메일입니다.");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 🟢 회원가입
     */
    @Operation(summary = "🟢 회원가입", description = "신규 회원 가입 처리")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/signup")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(
            @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {

        log.info("[AUTH] 회원가입 요청");

        // 회원가입 처리
        Map<String, Object> result = authService.signup(requestVo);

        // 성공 응답
        ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "회원가입이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 🟢 세션 체크
     */
    @Operation(summary = "🟢 세션 체크", description = "현재 세션 유효성 검증 및 사용자 정보 조회")
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSession(
            HttpServletRequest request) throws Exception {

        log.info("[AUTH-V1] 세션 체크 요청");

        HttpSession session = request.getSession(false);
        if (session == null) {
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.unauthorized("세션이 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        // 세션 정보 조회
        Long memNo = (Long) session.getAttribute("memNo");
        String loginId = (String) session.getAttribute("loginId");
        String memNm = (String) session.getAttribute("memNm");
        String mobileNo = (String) session.getAttribute("mobileNo");
        String authCd = (String) session.getAttribute("authCd");
        if (memNm == null) {
            memNm = (String) session.getAttribute("name"); // 호환성을 위해 name도 확인
        }

        if (memNo == null || loginId == null) {
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        // 응답 생성 (기본 정보)
        Map<String, Object> sessionResponse = new HashMap<>();
        sessionResponse.put("memNo", memNo);
        sessionResponse.put("loginId", loginId);
        sessionResponse.put("memNm", memNm);
        sessionResponse.put("mobileNo", mobileNo);
        sessionResponse.put("authCd", authCd);
        sessionResponse.put("sessionId", session.getId());
        sessionResponse.put("valid", true);

        // 추가 세션 정보 (카카오 로그인 등)
        String emailAddr = (String) session.getAttribute("emailAddr");
        if (emailAddr != null) {
            sessionResponse.put("emailAddr", emailAddr);
        }
        
        String memType = (String) session.getAttribute("memType");
        if (memType != null) {
            sessionResponse.put("memType", memType);
        }
        
        String loginType = (String) session.getAttribute("loginType");
        if (loginType != null) {
            sessionResponse.put("loginType", loginType);
        }
        
        Object loginTime = session.getAttribute("loginTime");
        if (loginTime != null) {
            sessionResponse.put("loginTime", loginTime);
        }
        
        // 카카오 관련 정보
        String kakaoId = (String) session.getAttribute("kakaoId");
        if (kakaoId != null) {
            sessionResponse.put("kakaoId", kakaoId);
        }
        
        // 카카오 액세스 토큰은 보안상 반환하지 않음 (필요시 별도 엔드포인트 사용)
        // String kakaoAccessToken = (String) session.getAttribute("kakaoAccessToken");

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(sessionResponse);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 🟢 아이디 찾기
     */
    @Operation(summary = "🟢 아이디 찾기", description = "이름과 휴대폰 번호로 아이디 찾기")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/find-id")
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findId(
        @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {

        log.info("[AUTH] 아이디 찾기 요청");

        try {
            // 아이디 찾기 처리
            Map<String, Object> result = authService.findId(requestVo);

            // 성공 응답
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "아이디를 찾았습니다.");
            return ResponseEntity.ok(response);
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[AUTH] 아이디 찾기 검증 실패: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[AUTH] 아이디 찾기 중 오류 발생", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error("아이디 찾기 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 🟢 비밀번호 찾기
     */
    @Operation(summary = "🟢 비밀번호 찾기", description = "아이디와 휴대폰 번호 인증 후 비밀번호 변경 권한 부여")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/find-password")
    @PostMapping("/find-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findPassword(
            @RequestBody MemberVO requestVo,
            HttpServletRequest request) throws Exception {

        log.info("[AUTH] 비밀번호 찾기 요청");

        try {
            // 비밀번호 찾기 처리
            Map<String, Object> result = authService.findPassword(requestVo, request);

            // 성공 응답
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "인증이 완료되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[AUTH] 비밀번호 찾기 검증 실패: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[AUTH] 비밀번호 찾기 중 오류 발생", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error("비밀번호 찾기 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 🟢 비밀번호 변경
     */
    @Operation(summary = "🟢 비밀번호 변경", description = "임시 비밀번호를 새 비밀번호로 변경")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/change-password")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changePassword(
        @RequestBody MemberVO requestVo,
        HttpServletRequest request) throws Exception {

        log.info("[AUTH] 비밀번호 변경 요청");

        try {
            // 비밀번호 변경 처리
            Map<String, Object> result = authService.changePassword(requestVo, request);

            // 성공 응답
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "비밀번호가 성공적으로 변경되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[AUTH] 비밀번호 변경 검증 실패: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[AUTH] 비밀번호 변경 중 오류 발생", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🟢 NIBS 계약 로그인 API 호출
     */
    @Operation(summary = "🟢 NIBS 계약 로그인", description = "NIBS 계약 로그인 API 호출")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/nibs-contract-login")
    @PostMapping("/nibs-contract-login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> nibsContractLogin(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) throws Exception {

        log.info("[AUTH] NIBS 계약 로그인 요청");

        try {
            // 입력값 검증
            String userName = requestBody.get("userName");
            String userId = requestBody.get("loginId");
            String pwd = requestBody.get("password");
            String phoneNumber = requestBody.get("phoneNumber");

            if (userId == null || userId.trim().isEmpty()) {
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("사용자 ID를 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (pwd == null || pwd.trim().isEmpty()) {
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("비밀번호를 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // NIBS 계약 로그인 API 호출
            Map<String, Object> result = authService.callNibsContractLogin(userId, pwd, userName, phoneNumber, request);

            // 성공 응답
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "NIBS 계약 로그인 API 호출 성공");
            return ResponseEntity.ok(response);

        } catch (jakarta.validation.ValidationException e) {
            log.warn("[AUTH] NIBS 계약 로그인 검증 실패: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("[AUTH] NIBS 계약 로그인 중 오류 발생", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error("NIBS 계약 로그인 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
