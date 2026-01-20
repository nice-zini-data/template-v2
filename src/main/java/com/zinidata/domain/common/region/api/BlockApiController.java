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
 * 블록 정보 API 컨트롤러
 * 
 * <p>블록(tbshp_block_v3_features) 관련 API를 처리합니다.</p>
 * <p>블록 단위 공간 검색 기능을 담당합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[region] 블록 관리", description = "블록 정보 조회 API - 블록 단위 공간 검색")
@Slf4j
@RestController
@RequestMapping("/api/common/region/block")
@RequiredArgsConstructor
public class BlockApiController {

    private final RegionService regionService;

    /**
     * 블록 코드로 블록 정보 조회
     * 
     * <p>6자리 블록 코드로 해당 블록의 상세 정보를 조회합니다.</p>
     * <p>블록명, 좌표, 폴리곤 경계 정보 등을 제공합니다.</p>
     * 
     * @param blkCd 블록 코드 (6자리)
     * @return 블록 정보 응답
     */
    @Operation(summary = "블록 코드 조회", description = "블록 코드(6자리)로 블록 정보 및 폴리곤 경계를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/block/code")
    @GetMapping("/{blkCd}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBlockByCode(
            @Parameter(description = "블록 코드 (6자리)", example = "110001", required = true)
            @PathVariable String blkCd) throws Exception {
        
        log.info("[REGION-V1] 블록 코드 조회 요청 - 블록코드: {}", blkCd);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> blockData = regionService.getBlockByCode(blkCd);
        
        log.info("[REGION-V1] 블록 코드 조회 성공 - 블록코드: {}", blkCd);
        return ResponseEntity.ok(ApiResponse.success(blockData, "블록 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 좌표로 블록 조회
     * 
     * <p>지도상 특정 좌표(위도, 경도)로 해당 위치의 블록 정보를 조회합니다.</p>
     * <p>블록 단위 위치 확인에 사용됩니다.</p>
     * 
     * @param lat 위도 (WGS84 좌표계)
     * @param lng 경도 (WGS84 좌표계)
     * @return 해당 좌표의 블록 정보 응답
     */
    @Operation(summary = "좌표 기반 블록 조회", description = "지도상 좌표(위도, 경도)로 해당 위치의 블록 정보를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/block/point")
    @GetMapping("/by-point")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBlockByPoint(
            @Parameter(description = "위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam double lat,
            @Parameter(description = "경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam double lng) throws Exception {
        
        log.info("[REGION-V1] 좌표 기반 블록 조회 요청 - 위도: {}, 경도: {}", lat, lng);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        Map<String, Object> blockData = regionService.getBlockByPoint(lat, lng);
        
        log.info("[REGION-V1] 좌표 기반 블록 조회 성공 - 위도: {}, 경도: {}", lat, lng);
        return ResponseEntity.ok(ApiResponse.success(blockData, "좌표 기반 블록 조회가 완료되었습니다."));
    }

    /**
     * 폴리곤 영역 내 블록 조회
     * 
     * <p>지정된 GeoJSON 폴리곤 영역 내에 포함되는 모든 블록 목록을 조회합니다.</p>
     * <p>지도상에서 사용자가 그린 영역 내의 블록들을 확인할 때 사용됩니다.</p>
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 폴리곤 영역에 포함되는 블록 목록 응답
     */
    @Operation(summary = "폴리곤 영역 블록 조회", description = "지정된 폴리곤 영역 내에 포함되는 모든 블록 목록을 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/block/polygon")
    @GetMapping("/by-polygon")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBlocksByPolygon(
            @Parameter(description = "GeoJSON 폴리곤 문자열", required = true)
            @RequestParam String polygon) throws Exception {
        log.info("[REGION-V1] 폴리곤 영역 블록 조회 - 폴리곤 크기: {}", polygon.length());
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> blocks = regionService.getBlocksByPolygon(polygon);
        
        log.info("[REGION-V1] 폴리곤 영역 블록 조회 성공 - 조회건수: {}", blocks.size());
        return ResponseEntity.ok(ApiResponse.success(blocks, "폴리곤 영역 블록 조회가 완료되었습니다."));
    }

    /**
     * 반경 내 블록 조회
     * 
     * <p>지정된 좌표에서 반경 내에 있는 블록 목록을 조회합니다.</p>
     * <p>최대 1km 반경 내의 블록들을 확인할 때 사용됩니다.</p>
     * 
     * @param lat 중심점 위도
     * @param lng 중심점 경도
     * @param radius 반경 (미터, 최대 1000m)
     * @return 반경 내 블록 목록 응답
     */
    @Operation(summary = "반경 내 블록 조회", description = "지정된 좌표에서 반경 내에 있는 블록 목록을 조회합니다 (최대 1km)")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/region/block/radius")
    @GetMapping("/by-radius")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBlocksByRadius(
            @Parameter(description = "중심점 위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam double lat,
            @Parameter(description = "중심점 경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam double lng,
            @Parameter(description = "반경 (미터, 최대 1000m)", example = "500")
            @RequestParam(defaultValue = "500") int radius) throws Exception {
        
        log.info("[REGION-V1] 반경 내 블록 조회 - 위도: {}, 경도: {}, 반경: {}m", lat, lng, radius);
        
        // 비즈니스 로직 처리 (검증과 조회는 서비스에서 담당)
        List<Map<String, Object>> blocks = regionService.getBlocksByRadius(lat, lng, radius);
        
        log.info("[REGION-V1] 반경 내 블록 조회 성공 - 위도: {}, 경도: {}, 반경: {}m, 조회건수: {}", lat, lng, radius, blocks.size());
        return ResponseEntity.ok(ApiResponse.success(blocks, "반경 내 블록 조회가 완료되었습니다."));
    }
}