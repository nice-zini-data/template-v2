package com.zinidata.domain.common.region.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
 * 광역권 정보 API 컨트롤러
 * 
 * <p>광역권(tbshp_mega_features) 관련 API를 처리합니다.</p>
 * <p>수도권/영남권 등 대권역 정보 관리 및 광역권별 공간 검색 기능을 담당합니다.</p>
 * <p>시도 목록 조회 API 제공</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[region] 광역권 관리", description = "광역권 정보 조회 API - 수도권/영남권 등 대권역 공간 검색")
@Slf4j
@RestController
@RequestMapping("/api/common/region/mega")
@RequiredArgsConstructor
public class MegaApiController {

    private final RegionService regionService;

    /**
     * 광역권 코드로 광역권 정보 조회
     * 
     * <p>광역권 코드로 해당 광역권의 상세 정보를 조회합니다.</p>
     * <p>광역권명, 좌표, 폴리곤 경계 정보 등을 제공합니다.</p>
     * 
     * @param megaCd 광역권 코드
     * @return 광역권 정보 응답
     */
    @Operation(summary = "🔴 광역권 코드 조회 [미사용]", description = "🔴 **미사용 API** - 광역권 코드로 광역권 정보 및 폴리곤 경계를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/mega/code")
    @GetMapping("/{megaCd}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMegaByCode(
            @Parameter(description = "광역권 코드", example = "01", required = true)
            @PathVariable String megaCd) throws Exception {
        
        log.info("[REGION-V1] 광역권 코드 조회 요청 - 광역권코드: {}", megaCd);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> megaData = regionService.getMegaByCode(megaCd);
        
        log.info("[REGION-V1] 광역권 코드 조회 성공 - 광역권코드: {}", megaCd);
        return ResponseEntity.ok(ApiResponse.success(megaData, "광역권 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 좌표로 광역권 조회
     * 
     * <p>지도상 특정 좌표(위도, 경도)로 해당 위치의 광역권 정보를 조회합니다.</p>
     * <p>수도권/영남권 등 대권역 중 해당 좌표가 속한 광역권을 확인할 때 사용됩니다.</p>
     * 
     * @param lat 위도 (WGS84 좌표계)
     * @param lng 경도 (WGS84 좌표계)
     * @return 해당 좌표의 광역권 정보 응답
     */
    @Operation(summary = "🔴 좌표 기반 광역권 조회 [미사용]", description = "🔴 **미사용 API** - 지도상 좌표(위도, 경도)로 해당 위치의 광역권 정보를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/mega/point")
    @GetMapping("/by-point")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMegaByPoint(
            @Parameter(description = "위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam double lat,
            @Parameter(description = "경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam double lng) throws Exception {
        
        log.info("[REGION-V1] 좌표 기반 광역권 조회 요청 - 위도: {}, 경도: {}", lat, lng);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> megaData = regionService.getMegaByPoint(lat, lng);
        
        log.info("[REGION-V1] 좌표 기반 광역권 조회 성공 - 위도: {}, 경도: {}", lat, lng);
        return ResponseEntity.ok(ApiResponse.success(megaData, "좌표 기반 광역권 조회가 완료되었습니다."));
    }

    /**
     * 폴리곤 영역 내 광역권 조회
     * 
     * <p>지정된 GeoJSON 폴리곤 영역 내에 포함되는 모든 광역권 목록을 조회합니다.</p>
     * <p>지도상에서 사용자가 그린 영역 내의 광역권들을 확인할 때 사용됩니다.</p>
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 폴리곤 영역에 포함되는 광역권 목록 응답
     */
    @Operation(summary = "🔴 폴리곤 영역 광역권 조회 [미사용]", description = "🔴 **미사용 API** - 지정된 폴리곤 영역 내에 포함되는 모든 광역권 목록을 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/mega/polygon")
    @GetMapping("/by-polygon")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMegasByPolygon(
            @Parameter(description = "GeoJSON 폴리곤 문자열", required = true)
            @RequestParam String polygon) throws Exception {
        log.info("[REGION-V1] 폴리곤 영역 광역권 조회 - 폴리곤 크기: {}", polygon.length());
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> megas = regionService.getMegasByPolygon(polygon);
        
        log.info("[REGION-V1] 폴리곤 영역 광역권 조회 성공 - 조회건수: {}", megas.size());
        return ResponseEntity.ok(ApiResponse.success(megas, "폴리곤 영역 광역권 조회가 완료되었습니다."));
    }

    /**
     * 시도 목록 조회
     * 
     * <p>전국 시도 코드와 이름 목록을 조회합니다.</p>
     * <p>시도 선택 UI 구성에 사용됩니다.</p>
     * 
     * @return 시도 목록 응답
     */
    @Operation(summary = "시도 목록 조회", description = "전국 시도 코드와 이름 목록을 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/mega/list")
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMegaList() throws Exception {
        
        log.info("[REGION-V1] 시도 목록 조회 요청");
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> megaList = regionService.getMegaList();
        
        log.info("[REGION-V1] 시도 목록 조회 성공 - 조회건수: {}", megaList.size());
        return ResponseEntity.ok(ApiResponse.success(megaList, "시도 목록 조회가 완료되었습니다."));
    }
}