package com.zinidata.domain.common.region.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.common.region.service.RegionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시도 정보 API 컨트롤러
 * 
 * <p>시도(tbshp_cty_features) 관련 API를 처리합니다.</p>
 * <p>전국 17개 시도 정보 관리 및 시도별 공간 검색 기능을 담당합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[region] 시도 관리", description = "시도 정보 조회 API - 전국 17개 시도 공간 검색")
@Slf4j
@RestController
@RequestMapping("/api/common/region/cty")
@RequiredArgsConstructor
public class CtyApiController {

    private final RegionService regionService;

    /**
     * 시도 코드로 시도 정보 조회
     * 
     * <p>2자리 시도 코드로 해당 시도의 상세 정보를 조회합니다.</p>
     * <p>시도명, 좌표, 폴리곤 경계 정보 등을 제공합니다.</p>
     * 
     * @param ctyCd 시도 코드 (2자리)
     * @return 시도 정보 응답
     */
    @Operation(summary = "시도 코드 조회", description = "시도 코드(2자리)로 시도 정보 및 폴리곤 경계를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/cty/code")
    @GetMapping("/{ctyCd}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCtyByCode(
            @Parameter(description = "시도 코드 (2자리)", example = "11", required = true)
            @PathVariable String ctyCd) throws Exception {
        
        log.info("[REGION-V1] 시도 코드 조회 요청 - 시도코드: {}", ctyCd);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> ctyData = regionService.getCtyByCode(ctyCd);
        
        log.info("[REGION-V1] 시도 코드 조회 성공 - 시도코드: {}", ctyCd);
        return ResponseEntity.ok(ApiResponse.success(ctyData, "시도 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 좌표로 시도 조회
     * 
     * <p>지도상 특정 좌표(위도, 경도)로 해당 위치의 시도 정보를 조회합니다.</p>
     * <p>전국 17개 시도 중 해당 좌표가 속한 시도를 확인할 때 사용됩니다.</p>
     * 
     * @param lat 위도 (WGS84 좌표계)
     * @param lng 경도 (WGS84 좌표계)
     * @return 해당 좌표의 시도 정보 응답
     */
    @Operation(summary = "좌표 기반 시도 조회", description = "지도상 좌표(위도, 경도)로 해당 위치의 시도 정보를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/cty/point")
    @GetMapping("/by-point")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCtyByPoint(
            @Parameter(description = "위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam double lat,
            @Parameter(description = "경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam double lng) throws Exception {
        
        log.info("[REGION-V1] 좌표 기반 시도 조회 요청 - 위도: {}, 경도: {}", lat, lng);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> ctyData = regionService.getCtyByPoint(lat, lng);
        
        log.info("[REGION-V1] 좌표 기반 시도 조회 성공 - 위도: {}, 경도: {}", lat, lng);
        return ResponseEntity.ok(ApiResponse.success(ctyData, "좌표 기반 시도 조회가 완료되었습니다."));
    }

    /**
     * 폴리곤 영역 내 시도 조회
     * 
     * <p>지정된 GeoJSON 폴리곤 영역 내에 포함되는 모든 시도 목록을 조회합니다.</p>
     * <p>지도상에서 사용자가 그린 영역 내의 시도들을 확인할 때 사용됩니다.</p>
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 폴리곤 영역에 포함되는 시도 목록 응답
     */
    @Operation(summary = "폴리곤 영역 시도 조회", description = "지정된 폴리곤 영역 내에 포함되는 모든 시도 목록을 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/cty/polygon")
    @GetMapping("/by-polygon")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCtysByPolygon(
            @Parameter(description = "GeoJSON 폴리곤 문자열", required = true)
            @RequestParam String polygon) throws Exception {
        log.info("[REGION-V1] 폴리곤 영역 시도 조회 - 폴리곤 크기: {}", polygon.length());
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> ctys = regionService.getCtysByPolygon(polygon);
        
        log.info("[REGION-V1] 폴리곤 영역 시도 조회 성공 - 조회건수: {}", ctys.size());
        return ResponseEntity.ok(ApiResponse.success(ctys, "폴리곤 영역 시도 조회가 완료되었습니다."));
    }
}