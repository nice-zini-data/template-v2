package com.zinidata.domain.home.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.home.service.HomeService;
import com.zinidata.domain.home.vo.HomeStatsVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

/**
 * 홈 도메인 API 컨트롤러
 * 
 * <p>홈 페이지 관련 API를 제공합니다.</p>
 * <p>통계 정보 조회, 반경 내 서비스 요청 수 조회 등의 기능을 포함합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[home] 홈 관리", description = "홈 페이지 관련 API - 통계 정보 조회, 반경 내 서비스 요청 수 조회")
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Slf4j
public class HomeApiController {

    private final HomeService homeService;

    /**
     * 🟢 홈 통계 정보 조회
     */
    @Operation(summary = "🟢 홈 통계 정보 조회", description = "반경 내 서비스 요청 수, 오늘 서비스 요청 수, 사용자별 통계 정보 조회")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/home/stats")
    @PostMapping("/stats")
    public ResponseEntity<ApiResponse<HomeStatsVO>> getHomeStats(
            @RequestBody HomeStatsVO requestVo,
            HttpServletRequest request) throws Exception {
        
        log.info("[HOME_API] 홈 통계 정보 조회 요청 - centerX: {}, centerY: {}, radius: {}", 
                requestVo.getCenterX(), requestVo.getCenterY(), requestVo.getRadius());
        
        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[HOME_API] 세션이 존재하지 않습니다.");
                ApiResponse<HomeStatsVO> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[HOME_API] 세션에 memNo가 없습니다.");
                ApiResponse<HomeStatsVO> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // requestVo에 memNo 설정
            requestVo.setMemNo(memNo.toString());
            
            log.info("[HOME_API] 세션에서 memNo 조회 완료 - memNo: {}", memNo);
            
            // 홈 통계 정보 조회
            HomeStatsVO stats = homeService.getHomeStats(requestVo);
            
            // 성공 응답
            ApiResponse<HomeStatsVO> response = ApiResponse.success(stats, "홈 통계 정보를 성공적으로 조회했습니다.");
            return ResponseEntity.ok(response);
            
        } catch (jakarta.validation.ValidationException e) {
            log.warn("[HOME_API] 홈 통계 정보 조회 검증 실패: {}", e.getMessage());
            ApiResponse<HomeStatsVO> response = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("[HOME_API] 홈 통계 정보 조회 중 오류 발생", e);
            ApiResponse<HomeStatsVO> response = ApiResponse.error("홈 통계 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
