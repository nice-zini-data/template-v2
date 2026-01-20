package com.zinidata.domain.requests.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.requests.service.MapService;
import com.zinidata.domain.requests.vo.MapVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 맵 조회 API 컨트롤러
 * 
 * <p>맵 조회 관련 API를 제공합니다.</p>
 * <p>block, admi, cty, mega 구분에 따른 맵 데이터를 조회합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[requests] 맵 조회", description = "요청 맵 조회 API - block, admi, cty, mega 구분별 조회")
@RestController
@RequestMapping("/api/requests/map")
@RequiredArgsConstructor
@Slf4j
public class MapApiController {

    private final MapService mapService;

    /**
     * 🟢 요청 맵 조회
     */
    @Operation(summary = "🟢 요청 맵 조회", description = "요청 맵을 조회합니다. 구분(block, admi, cty, mega)에 따라 다른 데이터를 반환합니다.")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/requests/map")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestMap(
            @RequestBody MapVO mapVO,
            HttpServletRequest request) {
        
        log.info("[MAP_API] 요청 맵 조회 요청 - gubun: {}, minx: {}, miny: {}, maxx: {}, maxy: {}", 
                mapVO.getGubun(), mapVO.getMinx(), mapVO.getMiny(), mapVO.getMaxx(), mapVO.getMaxy());
        
        try {
            // 세션에서 memNo 가져오기
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("[MAP_API] 세션이 존재하지 않습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long memNo = (Long) session.getAttribute("memNo");
            if (memNo == null) {
                log.warn("[MAP_API] 세션에 memNo가 없습니다.");
                ApiResponse<Map<String, Object>> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            log.info("[MAP_API] 세션에서 memNo 조회 완료 - memNo: {}", memNo);
            
            // 구분 검증
            if (mapVO.getGubun() == null || mapVO.getGubun().trim().isEmpty()) {
                log.warn("[MAP_API] 구분 값이 없습니다.");
                return ResponseEntity.ok(ApiResponse.badRequest("구분(gubun) 값은 필수입니다."));
            }
            
            // 좌표 검증
            if (mapVO.getMinx() == null || mapVO.getMiny() == null || 
                    mapVO.getMaxx() == null || mapVO.getMaxy() == null) {
                log.warn("[MAP_API] 좌표 값이 없습니다.");
                return ResponseEntity.ok(ApiResponse.badRequest("좌표 값은 필수입니다."));
            }
            
            // 맵 조회
            Map<String, Object> result = mapService.getRequestMap(mapVO);
            
            return ResponseEntity.ok(ApiResponse.success(result, "요청 맵 조회가 완료되었습니다."));
            
        } catch (IllegalArgumentException e) {
            log.warn("[MAP_API] 요청 맵 조회 검증 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("[MAP_API] 요청 맵 조회 중 오류 발생", e);
            return ResponseEntity.ok(ApiResponse.error("요청 맵 조회 중 오류가 발생했습니다."));
        }
    }
}

