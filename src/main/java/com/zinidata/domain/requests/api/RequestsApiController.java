package com.zinidata.domain.requests.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.common.util.AesCryptoUtil;
import com.zinidata.domain.requests.service.RequestService;
import com.zinidata.domain.requests.vo.RequestFileVO;
import com.zinidata.domain.requests.vo.RequestVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 요청 도메인 API 컨트롤러
 * 
 * <p>요청 등록 관련 API를 제공합니다.</p>
 * <p>신규 설치 요청, A/S 요청 등의 기능을 포함합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[requests] 요청 관리", description = "요청 등록 관련 API - 신규 설치 요청, A/S 요청")
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestsApiController {

    private final RequestService requestService;

    /**
     * 🟢 신규 설치 요청 등록
     */
    @Operation(summary = "🟢 신규 설치 요청 등록", description = "신규 설치 요청을 등록합니다.")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/install")
    @PostMapping("/install")
    public ResponseEntity<ApiResponse<RequestVO>> registerRequest(
            @RequestBody RequestVO requestVo,
            HttpServletRequest request) throws Exception {
        
        log.info("[REQUESTS_API] 신규 설치 요청 등록 요청 - crtName: {}, crtPhoneNumber: {}", 
                requestVo.getCrtName(), requestVo.getCrtPhoneNumber());
        
        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<RequestVO> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<RequestVO> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // requestVo에 memNo 설정
            requestVo.setMemNo(memNo);
            
            // 신규 설치 요청 등록
            RequestVO result = requestService.registerRequest(requestVo);
            
            if (result.getSuccess()) {
                // 성공 응답
                ApiResponse<RequestVO> response = ApiResponse.success(result, "신규 설치 요청이 성공적으로 등록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                // 등록 실패
                ApiResponse<RequestVO> response = ApiResponse.error("신규 설치 요청 등록에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[REQUESTS_API] 신규 설치 요청 등록 검증 실패: {}", e.getMessage());
            ApiResponse<RequestVO> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[REQUESTS_API] 신규 설치 요청 등록 중 오류 발생", e);
            ApiResponse<RequestVO> response = ApiResponse.error("신규 설치 요청 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Operation(summary = "🟢 신규 설치 요청 등록", description = "신규 설치 요청을 등록합니다.")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/install")
    @PostMapping("/as")
    public ResponseEntity<ApiResponse<RequestVO>> registerRequestAs(
            @RequestBody RequestVO requestVo,
            HttpServletRequest request) throws Exception {
        
        log.info("[REQUESTS_API] 신규 설치 요청 등록 요청 - crtName: {}, crtPhoneNumber: {}", 
                requestVo.getCrtName(), requestVo.getCrtPhoneNumber());
        
        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<RequestVO> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<RequestVO> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // requestVo에 memNo 설정
            requestVo.setMemNo(memNo);
            
            // 신규 설치 요청 등록
            RequestVO result = requestService.registerRequest(requestVo);
            
            if (result.getSuccess()) {
                // 성공 응답
                ApiResponse<RequestVO> response = ApiResponse.success(result, "신규 설치 요청이 성공적으로 등록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                // 등록 실패
                ApiResponse<RequestVO> response = ApiResponse.error("신규 설치 요청 등록에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[REQUESTS_API] 신규 설치 요청 등록 검증 실패: {}", e.getMessage());
            ApiResponse<RequestVO> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[REQUESTS_API] 신규 설치 요청 등록 중 오류 발생", e);
            ApiResponse<RequestVO> response = ApiResponse.error("신규 설치 요청 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🟢 파일 업로드
     */
    @Operation(summary = "🟢 파일 업로드", description = "요청에 첨부할 파일을 업로드합니다.")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/upload-files")
    @PostMapping("/upload-files")
    public ResponseEntity<ApiResponse<List<RequestFileVO>>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "requestSeq", required = false) String requestSeq,
            @RequestParam(value = "executeSw", required = false, defaultValue = "0") String executeSw,
            HttpServletRequest request) throws Exception {
        
        log.info("[REQUESTS_API] 파일 업로드 요청 - requestSeq: {}, executeSw: {}, fileCount: {}", 
                requestSeq, executeSw, files != null ? files.size() : 0);
        
        try {
            // requestSeq 검증
            if (requestSeq == null) {
                log.warn("[REQUESTS_API] requestSeq가 없습니다.");
                ApiResponse<List<RequestFileVO>> response = ApiResponse.badRequest("요청 번호(requestSeq)가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            requestSeq = AesCryptoUtil.decrypt(requestSeq);
            
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<List<RequestFileVO>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<List<RequestFileVO>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 파일 검증
            if (files == null || files.isEmpty()) {
                log.warn("[REQUESTS_API] 업로드할 파일이 없습니다.");
                ApiResponse<List<RequestFileVO>> response = ApiResponse.badRequest("업로드할 파일이 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 파일 개수 제한 (최대 3개)
            if (files.size() > 3) {
                log.warn("[REQUESTS_API] 파일 개수 초과 - 요청: {}, 허용: 3", files.size());
                ApiResponse<List<RequestFileVO>> response = ApiResponse.badRequest("최대 3개까지 업로드 가능합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 요청 정보 조회 (serviceGb 확인을 위해)
            // TODO: requestSeq로 RequestVO 조회하여 serviceGb 가져오기 (필요시 구현)
            String serviceGb = "1"; // 기본값: A/S (신규 설치는 "0")
            
            // 파일 업로드 및 정보 등록
            List<RequestFileVO> uploadedFiles = requestService.uploadFiles(files, requestSeq, memNo, serviceGb, executeSw);
            
            log.info("[REQUESTS_API] 파일 업로드 성공 - requestSeq: {}, uploadedCount: {}", 
                    requestSeq, uploadedFiles.size());
            
            ApiResponse<List<RequestFileVO>> response = ApiResponse.success(
                    uploadedFiles, 
                    uploadedFiles.size() + "개의 파일이 성공적으로 업로드되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[REQUESTS_API] 파일 업로드 중 오류 발생", e);
            ApiResponse<List<RequestFileVO>> response = ApiResponse.error("파일 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * 🟢 요청 내역 조회
     */
    @Operation(summary = "🟢 요청 내역 조회", description = "요청 내역 조회")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/history")
    @PostMapping("/history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestsHistory(
        @RequestBody RequestVO requestVO
    ) {
        log.info("요청 내역 조회 요청 - searchText: '{}', pageNo: {}, size: {}, sortType: {}, centerX: {}, centerY: {}", 
                requestVO.getSearchText(), requestVO.getPageNo(), requestVO.getSize(), requestVO.getSortType(), 
                requestVO.getCenterX(), requestVO.getCenterY());

        try {
            // 빈 문자열을 null로 변환
            if (requestVO.getSearchText() != null && requestVO.getSearchText().trim().isEmpty()) {
                requestVO.setSearchText(null);
            }
            
            List<RequestVO> requestsHistory = requestService.getRequestHistory(requestVO);

            Map<String, Object> result = new HashMap<>();
            result.put("requestsHistory", requestsHistory);

            log.info("요청 내역 조회 응답 - response: {}", result);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (ValidationException e) {
            log.error("[REQUESTS-API] 요청 내역 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS-API] 요청 내역 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("요청 내역 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 🟢 요청 상세 내역 조회
     */
    @Operation(summary = "🟢 요청 상세 내역 조회", description = "요청 상세 내역 조회")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/history-detail")
    @PostMapping("/history-detail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestHistoryDetail(
        @RequestBody RequestVO requestVO
    ) {
        log.info("[REQUESTS_API] 요청 상세 내역 조회 요청 - encryptedSeq: {}", requestVO.getEncryptedSeq());

        try {
            // encryptedSeq 검증 및 복호화
            if (requestVO.getEncryptedSeq() == null || requestVO.getEncryptedSeq().trim().isEmpty()) {
                log.warn("[REQUESTS_API] encryptedSeq가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("요청 번호(seq)가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 암호화된 seq 복호화
            String decryptedSeq;
            try {
                decryptedSeq = AesCryptoUtil.decrypt(requestVO.getEncryptedSeq());
                requestVO.setSeq(Long.parseLong(decryptedSeq));
            } catch (Exception e) {
                log.error("[REQUESTS_API] seq 복호화 실패 - encryptedSeq: {}", requestVO.getEncryptedSeq(), e);
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("유효하지 않은 요청 번호입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 요청 상세 정보 조회
            Map<String, Object> result = requestService.getRequestHistoryDetail(requestVO);

            log.info("[REQUESTS_API] 요청 상세 내역 조회 완료 - seq: {}", requestVO.getSeq());
            return ResponseEntity.ok(ApiResponse.success(result, "요청 상세 내역 조회가 완료되었습니다."));

        } catch (ValidationException e) {
            log.error("[REQUESTS_API] 요청 상세 내역 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS_API] 요청 상세 내역 조회 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error("요청 상세 내역 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 🟢 요청 취소
     */
    @Operation(summary = "🟢 요청 취소", description = "요청을 취소합니다.")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/cancel")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelRequest(
        @RequestBody RequestVO requestVO,
        HttpServletRequest request
    ) {
        log.info("[REQUESTS_API] 요청 취소 요청 - encryptedSeq: {}", requestVO.getEncryptedSeq());

        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // encryptedSeq 검증 및 복호화
            if (requestVO.getEncryptedSeq() == null || requestVO.getEncryptedSeq().trim().isEmpty()) {
                log.warn("[REQUESTS_API] encryptedSeq가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("요청 번호(seq)가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 암호화된 seq 복호화
            String decryptedSeq;
            try {
                decryptedSeq = AesCryptoUtil.decrypt(requestVO.getEncryptedSeq());
                requestVO.setSeq(Long.parseLong(decryptedSeq));
            } catch (Exception e) {
                log.error("[REQUESTS_API] seq 복호화 실패 - encryptedSeq: {}", requestVO.getEncryptedSeq(), e);
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("유효하지 않은 요청 번호입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // memNo 설정
            requestVO.setMemNo(memNo);
            
            // 요청 취소 처리
            RequestVO result = requestService.cancelRequest(requestVO);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("requestVO", result);
            
            if (result.getSuccess()) {
                log.info("[REQUESTS_API] 요청 취소 완료 - seq: {}", requestVO.getSeq());
                return ResponseEntity.ok(ApiResponse.success(responseData, "요청이 성공적으로 취소되었습니다."));
            } else {
                log.warn("[REQUESTS_API] 요청 취소 실패 - seq: {}", requestVO.getSeq());
                ApiResponse<Map<String, Object>> response = ApiResponse.error("요청 취소에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (ValidationException e) {
            log.error("[REQUESTS_API] 요청 취소 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS_API] 요청 취소 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error("요청 취소 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 🟢 수행 내역 조회
     */
    @Operation(summary = "🟢 수행 내역 조회", description = "수행 내역 조회")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/execute-history")
    @PostMapping("/execute-history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecuteHistory(
        @RequestBody RequestVO requestVO,
        HttpServletRequest request
    ) {
        log.info("수행 내역 조회 요청 - searchText: '{}', pageNo: {}, size: {}, sortType: {}, centerX: {}, centerY: {}", 
                requestVO.getSearchText(), requestVO.getPageNo(), requestVO.getSize(), requestVO.getSortType(), 
                requestVO.getCenterX(), requestVO.getCenterY());

        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // memNo 설정
            requestVO.setMemNo(memNo);
            
            // 빈 문자열을 null로 변환
            if (requestVO.getSearchText() != null && requestVO.getSearchText().trim().isEmpty()) {
                requestVO.setSearchText(null);
            }
            
            List<RequestVO> executeHistory = requestService.getExecuteHistory(requestVO);

            Map<String, Object> result = new HashMap<>();
            result.put("executeHistory", executeHistory);

            log.info("수행 내역 조회 응답 - response: {}", result);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (ValidationException e) {
            log.error("[REQUESTS-API] 수행 내역 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS-API] 수행 내역 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("요청 내역 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 🟢 요청 수락하기
     */
    @Operation(summary = "요청 수락", description = "요청 수락 하기")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/execute")
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeRequest(
        @RequestBody RequestVO requestVO,
        HttpServletRequest request
    ) {
        log.info("[REQUESTS_API] 요청 수락 - encryptedSeq: {}", requestVO.getEncryptedSeq());

        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // encryptedSeq 검증 및 복호화
            if (requestVO.getEncryptedSeq() == null || requestVO.getEncryptedSeq().trim().isEmpty()) {
                log.warn("[REQUESTS_API] encryptedSeq가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("요청 번호(seq)가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 암호화된 seq 복호화
            String decryptedSeq;
            try {
                decryptedSeq = AesCryptoUtil.decrypt(requestVO.getEncryptedSeq());
                requestVO.setSeq(Long.parseLong(decryptedSeq));
            } catch (Exception e) {
                log.error("[REQUESTS_API] seq 복호화 실패 - encryptedSeq: {}", requestVO.getEncryptedSeq(), e);
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("유효하지 않은 요청 번호입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // memNo 설정
            requestVO.setMemNo(memNo);
            requestVO.setExecId(memNo);
            
            // 요청 수락
            RequestVO result = requestService.executeRequest(requestVO);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("requestVO", result);
            
            if (result.getSuccess()) {
                log.info("[REQUESTS_API]요청 수락 완료");
                return ResponseEntity.ok(ApiResponse.success(responseData, "수행 내역이 성공적으로 저장되었습니다."));
            } else {
                log.warn("[REQUESTS_API] 요청 수락 실패 - seq: {}", requestVO.getSeq());
                ApiResponse<Map<String, Object>> response = ApiResponse.error("수행 내역 저장에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (ValidationException e) {
            log.error("[REQUESTS_API] 요청 수락 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS_API] 요청 수락 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error("수행 내역 저장 중 오류가 발생했습니다."));
        }
    }

    /**
     * 이미지 조회 (미리보기용)
     */
    @Operation(summary = "이미지 조회", description = "이미지 미리보기용 조회")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/view-image")
    @GetMapping("/view-image")
    public void viewImage(
            @RequestParam("fileNm") String fileNm,
            @RequestParam("filePath") String filePath,
            @RequestParam("orgFileNm") String orgFileNm,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            log.info("[REQUESTS_API] 이미지 조회 요청 - fileNm: {}, filePath: {}, orgFileNm: {}",
                    fileNm, filePath, orgFileNm);

            // 세션 체크
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 파라미터 검증
            if (fileNm == null || filePath == null || orgFileNm == null) {
                log.warn("[REQUESTS_API] 파일명, 파일경로, 원본파일명이 없습니다.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            RequestFileVO requestFileVO = RequestFileVO.builder()
                    .fileNm(fileNm)
                    .filePath(filePath)
                    .orgFileNm(orgFileNm)
                    .build();

            RequestFileVO result;
            try {
                result = requestService.selectFileByFileNmFilePathOrgFileNm(requestFileVO);
            } catch (FileNotFoundException e) {
                log.warn("[REQUESTS_API] 파일을 찾을 수 없습니다: {}", e.getMessage());
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            // 파일 경로 처리: filePath에 이미 전체 경로가 저장되어 있음
            String fullFilePath = result.getFilePath();
            if (fullFilePath == null || fullFilePath.isBlank()) {
                log.warn("[REQUESTS_API] 파일 경로가 없습니다.");
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                return;
            }

            // 파일 경로 정규화 및 존재 여부 확인
            Path filePathObj = Paths.get(fullFilePath).toAbsolutePath().normalize();
            
            if (!Files.exists(filePathObj)) {
                log.error("[REQUESTS_API] 파일이 존재하지 않습니다 - path: {}", filePathObj);
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            if (!Files.isReadable(filePathObj)) {
                log.error("[REQUESTS_API] 파일을 읽을 수 없습니다 - path: {}", filePathObj);
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }

            // Content-Type 결정 (이미지 타입 감지)
            String contentType = "image/png"; // 기본값
            try {
                String probe = Files.probeContentType(filePathObj);
                if (probe != null && probe.startsWith("image/")) {
                    contentType = probe;
                }
            } catch (IOException ignored) { }

            // 이미지 조회용 헤더 설정 (inline으로 표시)
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + orgFileNm.replace("\"", "") + "\"");
            response.setHeader("Cache-Control", "private, max-age=3600");

            // 파일 스트리밍
            byte[] files = Files.readAllBytes(filePathObj);
            response.setContentLength(files.length);
            response.getOutputStream().write(files);
            response.getOutputStream().flush();
            response.getOutputStream().close();

            log.info("[REQUESTS_API] 이미지 조회 완료 - orgFileNm: {}", orgFileNm);

        } catch (java.nio.file.NoSuchFileException e) {
            log.error("[REQUESTS_API] 파일을 찾을 수 없습니다 - path: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (java.nio.file.AccessDeniedException e) {
            log.error("[REQUESTS_API] 파일 접근 권한이 없습니다 - path: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("[REQUESTS_API] 이미지 조회 중 오류 발생", e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 파일 다운로드
     */
    @Operation(summary = "파일 다운로드", description = "파일 다운로드")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/download-files")
    @PostMapping("/download-files")
    public void downloadFiles(
            @RequestBody RequestFileVO requestFileVO,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            log.info("[REQUESTS_API] 파일 다운로드 요청 - fileNm: {}, filePath: {}, orgFileNm: {}",
                    requestFileVO.getFileNm(), requestFileVO.getFilePath(), requestFileVO.getOrgFileNm());

            // 세션 체크
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 파라미터 검증
            if (requestFileVO.getFileNm() == null || requestFileVO.getFilePath() == null || requestFileVO.getOrgFileNm() == null) {
                log.warn("[REQUESTS_API] 파일명, 파일경로, 원본파일명이 없습니다.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            RequestFileVO result;
            try {
                result = requestService.selectFileByFileNmFilePathOrgFileNm(requestFileVO);
            } catch (FileNotFoundException e) {
                log.warn("[REQUESTS_API] 파일을 찾을 수 없습니다: {}", e.getMessage());
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            // 파일 경로 처리: filePath에 이미 전체 경로가 저장되어 있음
            String fullFilePath = result.getFilePath();
            if (fullFilePath == null || fullFilePath.isBlank()) {
                log.warn("[REQUESTS_API] 파일 경로가 없습니다.");
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                return;
            }

            // 파일 경로 정규화 및 존재 여부 확인
            Path filePath = Paths.get(fullFilePath).toAbsolutePath().normalize();
            
            if (!Files.exists(filePath)) {
                log.error("[REQUESTS_API] 파일이 존재하지 않습니다 - path: {}", filePath);
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            if (!Files.isReadable(filePath)) {
                log.error("[REQUESTS_API] 파일을 읽을 수 없습니다 - path: {}", filePath);
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }

            // 파일 다운로드 처리
            byte[] files = Files.readAllBytes(filePath);

            response.setContentType("application/octet-stream");
            response.setContentLength(files.length);
            response.setHeader("Content-Disposition", "attachment; filename*='" + URLEncoder.encode(requestFileVO.getOrgFileNm(), StandardCharsets.UTF_8));
            response.setHeader("Content-Transfer-Encoding", "binary");
            
            response.getOutputStream().write(files);
            response.getOutputStream().flush();
            response.getOutputStream().close();

            log.info("[REQUESTS_API] 파일 다운로드 완료 - orgFileNm: {}", requestFileVO.getOrgFileNm());

        } catch (java.nio.file.NoSuchFileException e) {
            log.error("[REQUESTS_API] 파일을 찾을 수 없습니다 - path: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            // void 메서드이므로 예외를 다시 던지지 않음
        } catch (java.nio.file.AccessDeniedException e) {
            log.error("[REQUESTS_API] 파일 접근 권한이 없습니다 - path: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("[REQUESTS_API] 파일 다운로드 중 오류 발생", e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 🟢 완료 증빙 등록
     */
    @Operation(summary = "🟢 완료 증빙 등록", description = "완료 증빙 등록")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/completion")
    @PostMapping("/completion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completionRequest(
        @RequestBody RequestVO requestVO,
        HttpServletRequest request
    ) {
        log.info("[REQUESTS_API] 완료 증빙 등록 요청 - encryptedSeq: {}", requestVO.getEncryptedSeq());

        try {
            // 세션 체크
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // encryptedSeq 검증 및 복호화
            if (requestVO.getEncryptedSeq() == null || requestVO.getEncryptedSeq().trim().isEmpty()) {
                log.warn("[REQUESTS_API] encryptedSeq가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("요청 번호(seq)가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 암호화된 seq 복호화
            String decryptedSeq;
            try {
                decryptedSeq = AesCryptoUtil.decrypt(requestVO.getEncryptedSeq());
                requestVO.setSeq(Long.parseLong(decryptedSeq));
            } catch (Exception e) {
                log.error("[REQUESTS_API] seq 복호화 실패 - encryptedSeq: {}", requestVO.getEncryptedSeq(), e);
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("유효하지 않은 요청 번호입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // memNo 설정
            requestVO.setMemNo(memNo);
            requestVO.setExecId(memNo);

            // 완료 증빙 등록
            RequestVO result = requestService.completionRequest(requestVO);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("requestDetail", result);

            log.info("[REQUESTS_API] 완료 증빙 등록 완료 - seq: {}", requestVO.getSeq());
            return ResponseEntity.ok(ApiResponse.success(responseData, "완료 증빙 등록이 완료되었습니다."));

        } catch (ValidationException e) {
            log.error("[REQUESTS_API] 완료 증빙 등록 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS_API] 완료 증빙 등록 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error("완료 증빙 등록 중 오류가 발생했습니다."));
        }
    }

    /**
     * 🟢 요청 완료 처리
     */
    @Operation(summary = "🟢 요청 완료 처리", description = "요청을 완료 상태로 변경합니다.")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/done")
    @PostMapping("/done")
    public ResponseEntity<ApiResponse<Map<String, Object>>> doneRequest(
        @RequestBody RequestVO requestVO,
        HttpServletRequest request
    ) {
        log.info("[REQUESTS_API] 요청 완료 처리 요청 - encryptedSeq: {}", requestVO.getEncryptedSeq());

        try {
            // 세션 체크
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[REQUESTS_API] 세션이 존재하지 않습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[REQUESTS_API] 세션에 memNo가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // encryptedSeq 검증 및 복호화
            if (requestVO.getEncryptedSeq() == null || requestVO.getEncryptedSeq().trim().isEmpty()) {
                log.warn("[REQUESTS_API] encryptedSeq가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("요청 번호(seq)가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 암호화된 seq 복호화
            String decryptedSeq;
            try {
                decryptedSeq = AesCryptoUtil.decrypt(requestVO.getEncryptedSeq());
                requestVO.setSeq(Long.parseLong(decryptedSeq));
            } catch (Exception e) {
                log.error("[REQUESTS_API] seq 복호화 실패 - encryptedSeq: {}", requestVO.getEncryptedSeq(), e);
                ApiResponse<Map<String, Object>> response = ApiResponse.badRequest("유효하지 않은 요청 번호입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // memNo 설정
            requestVO.setMemNo(memNo);

            // 요청 완료 처리
            RequestVO result = requestService.doneRequest(requestVO);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("requestDetail", result);

            if (result != null && result.getSuccess()) {
                log.info("[REQUESTS_API] 요청 완료 처리 완료 - seq: {}", requestVO.getSeq());
                return ResponseEntity.ok(ApiResponse.success(responseData, "요청이 성공적으로 완료 처리되었습니다."));
            } else {
                log.warn("[REQUESTS_API] 요청 완료 처리 실패 - seq: {}", requestVO.getSeq());
                ApiResponse<Map<String, Object>> response = ApiResponse.error("요청 완료 처리에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (ValidationException e) {
            log.error("[REQUESTS_API] 요청 완료 처리 실패", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("[REQUESTS_API] 요청 완료 처리 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error("요청 완료 처리 중 오류가 발생했습니다."));
        }
    }
}

