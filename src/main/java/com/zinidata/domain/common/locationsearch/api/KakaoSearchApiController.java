package com.zinidata.domain.common.locationsearch.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.common.locationsearch.service.KakaoSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 카카오 검색 API 컨트롤러
 * 
 * <p>카카오 지도 API를 통한 위치 검색 기능을 제공합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[locationsearch] 🟢 위치 검색", description = "카카오 API를 통한 주소/키워드 검색 - 정확한 주소 조회 및 장소명 검색")
@Slf4j
@RestController
@RequestMapping("/api/common/location")
@RequiredArgsConstructor
public class KakaoSearchApiController {
    
    private final KakaoSearchService kakaoSearchService;
    
    /**
     * 주소 검색
     * 
     * @param query 검색 쿼리
     * @return 검색 결과
     */
    @Operation(summary = "✅ 주소 검색", description = "🟢 정확한 주소를 입력하여 위치 정보를 조회합니다\n\n" +
            "**사용처:** region-search.js → searchMixed() 호출 시 간접 사용\n\n" +
            "- 예시: '서울특별시 강남구 역삼동', '서울시 영등포구 여의도동'\n" +
            "- 도로명주소와 지번주소를 모두 지원\n" +
            "- 카카오 Local API의 주소 검색을 사용")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/location/search/address")
    @GetMapping("/search/address")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchByAddress(
            @Parameter(description = "검색할 주소 (예: 서울특별시 강남구 역삼동)", example = "서울시 영등포구 여의도동", required = true)
            @RequestParam String query) throws Exception {
        
        log.info("[LOCATION-V1] 주소 검색 API 호출: query={}", query);
        
        // 모든 검증과 처리는 서비스 레이어에서 수행 (예외는 GlobalExceptionHandler가 처리)
        Map<String, Object> result = kakaoSearchService.searchByAddress(query);
        
        return ResponseEntity.ok(ApiResponse.success(result, "주소 검색이 완료되었습니다."));
    }
    
    /**
     * 키워드 검색
     * 
     * @param query 검색 쿼리
     * @param x 중심 경도 (longitude, 선택)
     * @param y 중심 위도 (latitude, 선택)
     * @param radius 반경 거리 (미터, 최대 20000, 선택)
     * @return 검색 결과
     */
    @Operation(summary = "✅ 키워드 검색", description = "🟢 장소명이나 업체명으로 위치 정보를 조회합니다\n\n" +
            "**사용처:** region-search.js → searchMixed() 호출 시 간접 사용\n\n" +
            "- 예시: '스타벅스', '롯데월드타워', '강남역'\n" +
            "- 상호명, 브랜드명, 카테고리명으로 검색 가능\n" +
            "- 카카오 Local API의 키워드 검색을 사용\n\n" +
            "**반경 검색 옵션:**\n" +
            "- x, y, radius 파라미터를 제공하면 지정된 좌표 기준 반경 내에서 검색\n" +
            "- 결과는 거리순으로 정렬됨")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/location/search/keyword")
    @GetMapping("/search/keyword")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchByKeyword(
            @Parameter(description = "검색할 키워드 (예: 스타벅스, 롯데월드타워)", example = "스타벅스", required = true)
            @RequestParam String query,
            @Parameter(description = "중심 경도 (longitude, 선택)", example = "126.9780")
            @RequestParam(required = false) Double x,
            @Parameter(description = "중심 위도 (latitude, 선택)", example = "37.5665")
            @RequestParam(required = false) Double y,
            @Parameter(description = "반경 거리 (미터, 최대 20000, 선택)", example = "2000")
            @RequestParam(required = false) Integer radius) throws Exception {
        
        log.info("[LOCATION-V1] 키워드 검색 API 호출: query={}, x={}, y={}, radius={}", query, x, y, radius);
        
        // 모든 검증과 처리는 서비스 레이어에서 수행 (예외는 GlobalExceptionHandler가 처리)
        Map<String, Object> result;
        if (x != null && y != null && radius != null) {
            result = kakaoSearchService.searchByKeyword(query, x, y, radius);
        } else {
            result = kakaoSearchService.searchByKeyword(query);
        }
        
        return ResponseEntity.ok(ApiResponse.success(result, "키워드 검색이 완료되었습니다."));
    }
    
    /**
     * 통합 검색 (주소 + 키워드)
     * 
     * @param query 검색 쿼리
     * @param x 중심 경도 (longitude, 선택)
     * @param y 중심 위도 (latitude, 선택)
     * @param radius 반경 거리 (미터, 최대 20000, 선택)
     * @return 검색 결과
     */
    @Operation(summary = "✅ 통합 검색", description = "🟢 주소 검색과 키워드 검색을 동시에 수행하여 최대한 많은 결과를 제공합니다\n\n" +
            "**사용처:** Summary/Flowpop/Density 모든 탐색기에서 지역 검색 시 메인 API\n\n" +
            "- 주소와 키워드 검색을 모두 실행하여 결과를 통합\n" +
            "- 주소 정보와 장소 정보를 함께 반환\n" +
            "- 분석 지역 선택 시 추천하는 검색 방식\n\n" +
            "- 예시: '여의도역', '강남', '스타벅스 역삼점'\n\n" +
            "**반경 검색 옵션:**\n" +
            "- x, y, radius 파라미터를 제공하면 키워드 검색에만 반경 필터 적용\n" +
            "- 키워드 검색 결과는 거리순으로 정렬됨")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/location/search/mixed")
    @GetMapping("/search/mixed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchMixed(
            @Parameter(description = "검색할 주소 또는 키워드 (예: 여의도역, 강남, 스타벅스)", example = "여의도역", required = true)
            @RequestParam String query,
            @Parameter(description = "중심 경도 (longitude, 선택)", example = "126.9780")
            @RequestParam(required = false) Double x,
            @Parameter(description = "중심 위도 (latitude, 선택)", example = "37.5665")
            @RequestParam(required = false) Double y,
            @Parameter(description = "반경 거리 (미터, 최대 20000, 선택)", example = "2000")
            @RequestParam(required = false) Integer radius) throws Exception {
        
        log.info("[LOCATION-V1] 통합 검색 API 호출: query={}, x={}, y={}, radius={}", query, x, y, radius);
        
        // 모든 검증과 처리는 서비스 레이어에서 수행 (예외는 GlobalExceptionHandler가 처리)
        Map<String, Object> result;
        if (x != null && y != null && radius != null) {
            result = kakaoSearchService.searchMixed(query, x, y, radius);
        } else {
            result = kakaoSearchService.searchMixed(query);
        }
        
        return ResponseEntity.ok(ApiResponse.success(result, "통합 검색이 완료되었습니다."));
    }
    
    /**
     * 좌표→주소 변환 (역지오코딩)
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 주소 정보
     */
    @Operation(summary = "✅ 좌표→주소 변환", description = "🟢 위도/경도 좌표를 주소로 변환합니다 (역지오코딩)\n\n" +
            "**사용처:** 유동인구 페이지에서 지도 중심점 주소 표시\n\n" +
            "- 지도 중심점이나 검색 위치의 주소를 가져올 때 사용\n" +
            "- 도로명주소와 지번주소를 모두 반환\n" +
            "- 한국 영역 내 좌표만 지원 (위도: 33~43, 경도: 124~132)")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/location/coord2address")
    @GetMapping("/coord2address")
    public ResponseEntity<ApiResponse<Map<String, Object>>> coord2Address(
            @Parameter(description = "위도 (예: 37.5665)", example = "37.5665", required = true)
            @RequestParam double lat,
            @Parameter(description = "경도 (예: 126.9780)", example = "126.9780", required = true)
            @RequestParam double lng) throws Exception {
        
        log.info("[LOCATION-V1] 좌표→주소 변환 API 호출: lat={}, lng={}", lat, lng);
        
        // 모든 검증과 처리는 서비스 레이어에서 수행 (예외는 GlobalExceptionHandler가 처리)
        Map<String, Object> result = kakaoSearchService.coord2Address(lat, lng);
        
        return ResponseEntity.ok(ApiResponse.success(result, "좌표→주소 변환이 완료되었습니다."));
    }
} 