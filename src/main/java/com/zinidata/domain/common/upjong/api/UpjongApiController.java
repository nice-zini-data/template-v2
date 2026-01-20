package com.zinidata.domain.common.upjong.api;

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
import com.zinidata.domain.common.upjong.service.UpjongService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 분석업종 API 컨트롤러
 * 
 * <p>분석업종(upjong1/upjong2/upjong3) 관련 API를 처리합니다.</p>
 * <p>대분류, 중분류, 소분류로 구성된 3단계 업종 계층구조 조회와 업종명 검색 기능을 제공합니다.</p>
 * <p>행정동별 확장 점포수 기반 분석가능 여부 판단 기능도 지원합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Tag(name = "[upjong] 🟢 업종 관리", description = "분석업종 관련 API - 대/중/소분류 업종 조회 및 계층구조 관리")
@Slf4j
@RestController
@RequestMapping("/api/common/upjong")
@RequiredArgsConstructor
public class UpjongApiController {

    private final UpjongService upjongService;

    /**
     * 분석업종 목록 조회 (필터링 지원)
     * 
     * <p>지정된 레벨의 업종 목록을 조회합니다.</p>
     * <ul>
     *   <li>upjong1: 모든 대분류 업종</li>
     *   <li>upjong2: 모든 중분류 또는 특정 대분류의 중분류들</li>
     *   <li>upjong3: 모든 소분류 또는 특정 중분류의 소분류들</li>
     * </ul>
     * 
     * @param level 업종 레벨 (upjong1/upjong2/upjong3)
     * @param upjongCode 상위 업종 코드 (선택적, 레벨에 따라 필터링)
     * @return 업종 목록 응답
     */
    @Operation(summary = "✅ 분석업종 목록 조회", description = "🟢 **실제 사용 중인 API** - 지정된 레벨의 업종 목록을 조회합니다\n\n" +
            "- upjong1: 모든 대분류\n" +
            "- upjong2: 모든 중분류 또는 특정 대분류의 중분류들 (upjongCode = 대분류코드, 예: Q)\n" +
            "- upjong3: 모든 소분류 또는 특정 중분류의 소분류들 (upjongCode = 중분류코드, 예: Q13)")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/upjong/list")
    @GetMapping("/{level}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUpjongList(
            @Parameter(description = "업종 레벨 (upjong1: 대분류, upjong2: 중분류, upjong3: 소분류)", example = "upjong2", required = true)
            @PathVariable String level,
            @Parameter(description = "상위 업종 코드 (선택적, upjong1일 때는 무시됨)", example = "Q", required = false)
            @RequestParam(required = false) String upjongCode) throws Exception {
        
        log.info("[UPJONG-V1] 분석업종 목록 조회 요청 - 레벨: {}, 상위코드: {}", level, upjongCode);
        
        // 비즈니스 로직 처리 (예외는 GlobalExceptionHandler에서 처리)
        List<Map<String, Object>> upjongList = upjongService.getUpjongList(level, upjongCode);
        
        // 응답 데이터 구성
        Map<String, Object> responseData = Map.of(
            "level", level,
            "filterCode", upjongCode != null ? upjongCode : "ALL",
            "upjongs", upjongList,
            "totalCount", upjongList.size()
        );
        
        log.info("[UPJONG-V1] 분석업종 목록 조회 성공 - 레벨: {}, 조회건수: {}", level, upjongList.size());
        return ResponseEntity.ok(ApiResponse.success(responseData));
    }

    /**
     * 전체 업종 계층구조 조회
     * 
     * <p>대분류 > 중분류 > 소분류의 3단계 업종 계층구조를 조회합니다.</p>
     * <p>모든 업종 데이터를 트리 구조로 반환하며, 업종 선택 UI 구성에 사용됩니다.</p>
     * 
     * @return 3단계 업종 계층구조 응답
     */
    @Operation(summary = "✅ 업종 계층구조 조회", description = "🟢 **실제 사용 중인 API** - 대분류 > 중분류 > 소분류 전체 업종 계층구조를 조회합니다")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/upjong/hierarchy")
    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUpjongHierarchy() throws Exception {
        
        log.info("[UPJONG-V1] 전체 업종 계층구조 조회 요청");
        
        // 비즈니스 로직 처리 - 전체 계층구조 조회 (예외는 GlobalExceptionHandler에서 처리)
        Map<String, Object> hierarchyData = upjongService.getUpjongHierarchy();
        
        log.info("[UPJONG-V1] 전체 업종 계층구조 조회 성공 - 대분류 수: {}", 
                 hierarchyData != null ? hierarchyData.size() : 0);
        return ResponseEntity.ok(ApiResponse.success(hierarchyData, "업종 계층구조를 성공적으로 조회했습니다."));
    }

    /**
     * 필터링된 업종 계층구조 조회
     * 
     * <p>특정 업종 코드를 기준으로 필터링된 계층구조를 조회합니다.</p>
     * <ul>
     *   <li>1자리 코드 (예: Q): 해당 대분류의 하위 중분류/소분류</li>
     *   <li>3자리 코드 (예: Q13): 해당 중분류의 하위 소분류</li>
     *   <li>6자리 코드 (예: Q13007): 해당 소분류의 상세 정보</li>
     * </ul>
     * 
     * @param upjongCode 업종 코드 (길이에 따라 필터링 레벨 결정)
     * @return 필터링된 업종 계층구조 응답
     */
    @Operation(summary = "✅ 필터링된 업종 계층구조 조회", description = "🟢 **실제 사용 중인 API** - 특정 업종 코드에 해당하는 계층구조를 조회합니다\n\n" +
            "- 1자리 (예: Q): 대분류 필터링\n" +
            "- 3자리 (예: Q13): 중분류 필터링\n" +
            "- 6자리 (예: Q13007): 소분류 필터링")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/upjong/hierarchy/filter")
    @GetMapping("/hierarchy/{upjongCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUpjongHierarchyByCode(
            @Parameter(description = "업종 코드 (1자리: 대분류, 3자리: 중분류, 6자리: 소분류)", example = "Q13", required = true)
            @PathVariable String upjongCode) throws Exception {
        
        log.info("[UPJONG-V1] 필터링된 업종 계층구조 조회 요청 - 업종코드: {}", upjongCode);
        
        // 비즈니스 로직 처리 - 검증과 조회는 서비스에서 담당
        Map<String, Object> hierarchyData = upjongService.getUpjongHierarchy(upjongCode);
        
        log.info("[UPJONG-V1] 필터링된 업종 계층구조 조회 성공 - 업종코드: {}, 결과크기: {}", 
                 upjongCode, hierarchyData != null ? hierarchyData.size() : 0);
        return ResponseEntity.ok(ApiResponse.success(hierarchyData, "필터링된 업종 계층구조를 성공적으로 조회했습니다."));
    }

    /**
     * 업종명으로 업종 검색
     * 
     * <p>소분류 업종명으로 업종을 검색하고 관련 계층구조를 반환합니다.</p>
     * <ul>
     *   <li>검색어가 포함된 소분류 업종들을 찾아 해당 중분류의 전체 하위 업종을 반환</li>
     *   <li>검색된 업종은 highlighted: true로 표시</li>
     *   <li>행정동 코드 제공 시 확장 점포수 기준 분석가능 여부 포함</li>
     * </ul>
     * 
     * @param keyword 검색할 업종명 (소분류 업종명의 일부)
     * @param admiCd 행정동 코드 (선택적, 8자리)
     * @return 검색된 업종들의 계층구조 응답
     */
    @Operation(summary = "✅ 업종명 검색", description = "🟢 **메인 사용 API** - 소분류 업종명으로 업종을 검색하고 계층구조를 반환합니다\n\n" +
            "**사용처:** Summary/Flowpop/Density 모든 탐색기에서 업종 검색 시 메인 API\n\n" +
            "**호출 위치:** upjong-search.js에서 직접 호출\n\n" +
            "- keyword: '골프', '커피', '치킨' 등 소분류 업종명의 일부\n" +
            "- 검색된 업종들이 속한 중분류의 전체 하위 업종들을 계층구조로 반환\n" +
            "- 검색어가 포함된 업종은 highlighted: true 표시")
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/common/upjong/search")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchUpjongByName(
            @Parameter(description = "검색할 업종명 (예: 골프, 커피, 치킨)", example = "골프", required = true)
            @RequestParam String keyword,
            @Parameter(description = "선택 행정동 코드(8자리). 전달 시 확장 점포수 기준으로 analyzable 포함", example = "11680545")
            @RequestParam(required = false) String admiCd) throws Exception {
        
        log.info("[UPJONG-V1] 업종명 검색 요청 - 검색어: {}, 행정동코드: {}", keyword, admiCd);
        
        // 행정동 코드가 없는 경우 - 기본 검색 로직 사용
        if (admiCd == null || admiCd.trim().length() < 8) {
            Map<String, Object> searchResult = upjongService.searchUpjongByName(keyword);
            return ResponseEntity.ok(ApiResponse.success(searchResult, "업종 검색이 완료되었습니다."));
        }
        
        // 행정동 코드가 있는 경우 - 확장 점포수 기반 검색 (기존 복잡한 로직 보존)
        Map<String, Object> searchResult = upjongService.searchUpjongByNameWithStore(keyword, admiCd);
        return ResponseEntity.ok(ApiResponse.success(searchResult, "업종 검색이 완료되었습니다."));
    }
} 