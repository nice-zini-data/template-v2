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
 * 행정동 정보 API 컨트롤러
 * 
 * <p>행정동(tbshp_admi_features) 관련 API를 처리합니다.</p>
 * <p>좌표 기반 행정동 조회, 행정동별 GeoJSON 데이터 제공 등의 기능을 담당합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[region] 행정동 관리", description = "행정동 정보 조회 API - 좌표 기반 행정동 검색, GeoJSON 데이터 제공")
@Slf4j
@RestController
@RequestMapping("/api/common/region/admi")
@RequiredArgsConstructor
public class AdmiApiController {

    private final RegionService regionService;

    /**
     * 행정동 코드로 행정동 정보 조회
     * 
     * <p>8자리 행정동 코드로 해당 행정동의 상세 정보를 조회합니다.</p>
     * <p>행정동명, 좌표, 폴리곤 경계 정보 등을 제공합니다.</p>
     * 
     * @param admiCd 행정동 코드 (8자리)
     * @return 행정동 정보 응답
     */
    @Operation(summary = "🔴 행정동 코드 조회 [미사용]", description = "🔴 **미사용 API** - 행정동 코드(8자리)로 행정동 정보 및 폴리곤 경계를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/admi/code")
    @GetMapping("/{admiCd}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdmiByCode(
            @Parameter(description = "행정동 코드 (8자리)", example = "11680545", required = true)
            @PathVariable String admiCd) throws Exception {
        
        log.info("[REGION-V1] 행정동 코드 조회 요청 - 행정동코드: {}", admiCd);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> admiData = regionService.getAdmiByCode(admiCd);
        
        log.info("[REGION-V1] 행정동 코드 조회 성공 - 행정동코드: {}", admiCd);
        return ResponseEntity.ok(ApiResponse.success(admiData, "행정동 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 좌표로 행정동 조회
     * 
     * <p>지도상 특정 좌표(위도, 경도)로 해당 위치의 행정동 정보를 조회합니다.</p>
     * <p>지도 클릭 이벤트나 GPS 좌표를 통한 현재 위치 행정동 확인에 사용됩니다.</p>
     * 
     * @param lat 위도 (WGS84 좌표계)
     * @param lng 경도 (WGS84 좌표계)
     * @return 해당 좌표의 행정동 정보 응답
     */
    @Operation(summary = "✅ 좌표 기반 행정동 조회", description = "🟢 **메인 사용 API** - 지도상 좌표(위도, 경도)로 해당 위치의 행정동 정보를 조회합니다\n\n" +
            "**사용처:** Summary/Flowpop/Density 모든 탐색기에서 지역 선택 시 행정동 코드 조회용 메인 API\n\n" +
            "**호출 위치:** map-common.js와 density.js에서 직접 호출")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/admi/point")
    @GetMapping("/by-point")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdmiByPoint(
            @Parameter(description = "위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam double lat,
            @Parameter(description = "경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam double lng) throws Exception {
        
        log.info("[REGION-V1] 좌표 기반 행정동 조회 요청 - 위도: {}, 경도: {}", lat, lng);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> admiData = regionService.getAdmiByPoint(lat, lng);
        
        log.info("[REGION-V1] 좌표 기반 행정동 조회 성공 - 위도: {}, 경도: {}", lat, lng);
        return ResponseEntity.ok(ApiResponse.success(admiData, "좌표 기반 행정동 조회가 완료되었습니다."));
    }

    /**
     * 폴리곤 영역 내 행정동 조회
     * 
     * <p>지정된 GeoJSON 폴리곤 영역 내에 포함되는 모든 행정동 목록을 조회합니다.</p>
     * <p>지도상에서 사용자가 그린 영역 내의 행정동들을 확인할 때 사용됩니다.</p>
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 폴리곤 영역에 포함되는 행정동 목록 응답
     */
    @Operation(summary = "🔴 폴리곤 영역 행정동 조회 [미사용]", description = "🔴 **미사용 API** - 지정된 폴리곤 영역 내에 포함되는 모든 행정동 목록을 조회합니다\n\n" +
            "**현재 상태:** 실제로 사용되지 않는 API\n\n" +
            "- 지도상에서 사용자가 그린 영역 내의 행정동들을 확인할 때 사용")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/admi/polygon")
    @GetMapping("/by-polygon")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAdmisByPolygon(
            @Parameter(description = "GeoJSON 폴리곤 문자열", required = true)
            @RequestParam String polygon) throws Exception {
        log.info("[REGION-V1] 폴리곤 영역 행정동 조회 - 폴리곤 크기: {}", polygon.length());
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> admis = regionService.getAdmisByPolygon(polygon);
        
        log.info("[REGION-V1] 폴리곤 영역 행정동 조회 성공 - 조회건수: {}", admis.size());
        return ResponseEntity.ok(ApiResponse.success(admis, "폴리곤 영역 행정동 조회가 완료되었습니다."));
    }

    /**
     * 인접 행정동 조회
     * 
     * <p>기준 행정동과 경계를 공유하는 인접 행정동 목록을 조회합니다.</p>
     * <p>기준 행정동을 포함하여 인접한 모든 행정동의 정보를 제공합니다.</p>
     * 
     * @param admiCd 기준 행정동 코드 (8자리)
     * @return 기준 행정동과 인접한 행정동 목록 응답 (기준 행정동 포함)
     */
    @Operation(summary = "🔴 인접 행정동 조회 [미사용]", description = "🔴 **미사용 API** - 기준 행정동과 경계를 공유하는 인접 행정동 목록을 조회합니다 (기준 행정동 포함)")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/admi/neighbors")
    @GetMapping("/{admiCd}/neighbors")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAdjacentAdmis(
            @Parameter(description = "기준 행정동 코드 (8자리)", example = "11680545", required = true)
            @PathVariable String admiCd) throws Exception {
        log.info("[REGION-V1] 인접 행정동 조회 - 기준 행정동: {}", admiCd);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> neighbors = regionService.getAdjacentAdmis(admiCd);
        
        log.info("[REGION-V1] 인접 행정동 조회 성공 - 기준 행정동: {}, 조회건수: {}", admiCd, neighbors.size());
        return ResponseEntity.ok(ApiResponse.success(neighbors, "인접 행정동 조회가 완료되었습니다."));
    }
} 