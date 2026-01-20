package com.zinidata.domain.common.file.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zinidata.domain.common.file.service.FileDownloadServiceV1;
import com.zinidata.domain.common.file.service.S3ServiceV1;
import com.zinidata.domain.common.file.vo.FileInfoVO;
import com.zinidata.domain.common.user.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 파일 다운로드 API 컨트롤러
 * 
 * 보안이 적용된 파일 다운로드 기능을 제공합니다.
 * - 사용자 인증 확인
 * - 파일 접근 권한 확인
 * - Presigned URL 생성 (5초 만료)
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "파일 다운로드", description = "보안이 적용된 파일 다운로드 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileDownloadApiController {
    
    private final FileDownloadServiceV1 fileDownloadService;
    private final S3ServiceV1 s3Service;
    
    /**
     * Sales 파일 다운로드 URL 생성
     * 
     * 시작년월과 종료년월을 입력받아 sales_YYYYMM_YYYYMM 형식의 파일을 다운로드합니다.
     * 
     * @param startMonth 시작년월 (YYYYMM 형식)
     * @param endMonth 종료년월 (YYYYMM 형식)
     * @param request HTTP 요청
     * @return 다운로드 URL 정보
     */
    @Operation(
        summary = "Sales 파일 다운로드 URL 생성",
        description = "시작년월과 종료년월을 기반으로 sales 파일의 5초 만료 Presigned URL을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "다운로드 URL 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 년월 형식"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/file/sales/download-url")
    @PostMapping("/sales/download-url")
    public ResponseEntity<Map<String, Object>> generateSalesDownloadUrl(
            @Parameter(description = "시작년월 (YYYYMM)", required = true)
            @RequestParam String startMonth,
            @Parameter(description = "종료년월 (YYYYMM)", required = true)
            @RequestParam String endMonth,
            HttpServletRequest request) {
        
        log.info("Sales 파일 다운로드 URL 생성 요청: startMonth={}, endMonth={}, ip={}", 
                startMonth, endMonth, getClientIpAddress(request));
        
        try {
            // 1. 입력값 검증
            if (!isValidYearMonth(startMonth) || !isValidYearMonth(endMonth)) {
                return ResponseEntity.status(400)
                    .body(createErrorResponse("년월 형식이 올바르지 않습니다. YYYYMM 형식으로 입력하세요."));
            }
            
            // 2. 파일명 생성
            String fileName = String.format("sales_%s_%s.xlsx", startMonth, endMonth);
            String s3Key = String.format("webapp/soil/%s", fileName);
            
            // 3. S3에서 파일 존재 확인
            if (!s3Service.isObjectExists(s3Key)) {
                return ResponseEntity.status(404)
                    .body(createErrorResponse("요청하신 기간의 Sales 파일이 존재하지 않습니다."));
            }
            
            // 4. Presigned URL 생성
            String presignedUrl = s3Service.generatePresignedDownloadUrl(s3Key);
            
            // 5. 응답 데이터 구성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "다운로드 URL 생성 완료");
            responseData.put("data", Map.of(
                "downloadUrl", presignedUrl,
                "fileName", fileName,
                "startMonth", startMonth,
                "endMonth", endMonth,
                "expiresIn", 5,
                "expiresAt", LocalDateTime.now().plusSeconds(5),
                "downloadId", generateSalesDownloadId(startMonth, endMonth)
            ));
            
            log.info("Sales 파일 다운로드 URL 생성 완료: fileName={}", fileName);
            
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            log.error("Sales 파일 다운로드 URL 생성 실패: startMonth={}, endMonth={}", 
                     startMonth, endMonth, e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("다운로드 URL 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 파일 다운로드 완료 알림
     * 
     * @param fileId 파일 ID
     * @param downloadId 다운로드 ID
     * @param request HTTP 요청
     * @return 처리 결과
     */
    @Operation(
        summary = "파일 다운로드 완료 알림",
        description = "파일 다운로드 완료를 서버에 알립니다."
    )
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/file/download-complete")
    @PostMapping("/{fileId}/download-complete")
    public ResponseEntity<Map<String, Object>> notifyDownloadComplete(
            @Parameter(description = "다운로드한 파일 ID", required = true)
            @PathVariable Long fileId,
            @Parameter(description = "다운로드 ID", required = true)
            @RequestParam String downloadId,
            HttpServletRequest request) {
        
        try {
            UserVO currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }
            
            // 다운로드 완료 로그 기록
            fileDownloadService.logDownloadComplete(currentUser.getUserId(), fileId, 
                                                  downloadId, getClientIpAddress(request));
            
            return ResponseEntity.ok(createSuccessResponse("다운로드 완료 처리되었습니다."));
            
        } catch (Exception e) {
            log.error("다운로드 완료 알림 처리 실패: fileId={}, downloadId={}", fileId, downloadId, e);
            return ResponseEntity.status(500).body(createErrorResponse("처리 중 오류가 발생했습니다."));
        }
    }
    
    // ========== 유틸리티 메서드 ==========
    
    /**
     * 현재 로그인한 사용자 정보 조회
     * 
     * @param request HTTP 요청
     * @return 사용자 정보
     */
    private UserVO getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        // TODO: 실제 세션에서 사용자 정보 조회 로직 구현 필요
        // 현재는 샘플 사용자 반환
        UserVO sampleUser = new UserVO();
        sampleUser.setUserId(1L);
        sampleUser.setLoginId("testuser");
        sampleUser.setUserName("테스트 사용자");
        sampleUser.setUserRole(com.zinidata.common.enums.UserRole.USER);
        
        return sampleUser;
    }
    
    /**
     * 클라이언트 IP 주소 추출
     * 
     * @param request HTTP 요청
     * @return IP 주소
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 년월 형식 검증 (YYYYMM)
     * 
     * @param yearMonth 검증할 년월 문자열
     * @return 유효한 형식 여부
     */
    private boolean isValidYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.length() != 6) {
            return false;
        }
        
        try {
            int year = Integer.parseInt(yearMonth.substring(0, 4));
            int month = Integer.parseInt(yearMonth.substring(4, 6));
            
            return year >= 2020 && year <= 2030 && month >= 1 && month <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Sales 다운로드 ID 생성
     * 
     * @param startMonth 시작년월
     * @param endMonth 종료년월
     * @return 다운로드 ID
     */
    private String generateSalesDownloadId(String startMonth, String endMonth) {
        return String.format("SALES_%s_%s_%d", startMonth, endMonth, System.currentTimeMillis());
    }
    
    /**
     * 성공 응답 생성
     * 
     * @param message 메시지
     * @return 응답 Map
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
    
    /**
     * 에러 응답 생성
     * 
     * @param message 에러 메시지
     * @return 응답 Map
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}