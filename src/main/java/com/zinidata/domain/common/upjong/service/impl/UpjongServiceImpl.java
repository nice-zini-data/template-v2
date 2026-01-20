package com.zinidata.domain.common.upjong.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.domain.common.upjong.mapper.UpjongMapper;
import com.zinidata.domain.common.upjong.service.UpjongService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 데이터베이스 방식 분석업종 서비스 구현체 ⭐ 현재 사용 중
 * 
 * <p>MyBatis를 사용하여 데이터베이스에서 업종 정보를 조회합니다.</p>
 * 
 * <h3>활성화 조건</h3>
 * <ul>
 *   <li>application.yml에 upjong.datasource 설정이 없는 경우 (기본값)</li>
 *   <li>upjong.datasource: database로 명시적 설정한 경우</li>
 * </ul>
 * 
 * <h3>특징</h3>
 * <ul>
 *   <li>✅ <strong>장점:</strong> 데이터 정확성 100%, 구현 단순함, 디버깅 용이</li>
 *   <li>❌ <strong>단점:</strong> DB 부하 높음, 응답 속도 상대적으로 느림</li>
 * </ul>
 * 
 * <p><strong>Redis 구현체로 변경하려면:</strong> application.yml에 upjong.datasource: redis 추가</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "upjong.datasource", havingValue = "database", matchIfMissing = true)
@RequiredArgsConstructor
public class UpjongServiceImpl implements UpjongService {

    private final UpjongMapper upjongMapper;

    @Override
    public List<Map<String, Object>> getUpjongList(String level, String upjongCode) {
        log.debug("분석업종 목록 조회 - 레벨: {}, 코드: {}", level, upjongCode);
        
        // 입력값 검증
        if (level == null || level.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "업종 레벨은 필수입니다.");
        }
        
        return switch (level.toLowerCase()) {
            case "upjong1" -> upjongMapper.selectUpjong1List();  // 대분류는 상위 코드 무시
            case "upjong2" -> upjongMapper.selectUpjong2List(upjongCode);  // 특정 대분류의 중분류들
            case "upjong3" -> upjongMapper.selectUpjong3List(upjongCode);  // 특정 중분류의 소분류들
            default -> throw new IllegalArgumentException("지원하지 않는 업종 레벨입니다: " + level);
        };
    }

    @Override
    public Map<String, Object> getUpjongHierarchy(String upjongCode) {
        log.debug("업종 계층구조 조회 - 필터: {}", upjongCode);
        
        // 입력값 검증 및 업종 코드 분석
        String filterType = analyzeUpjongCode(upjongCode);
        
        // 업종 코드에 따른 계층구조 데이터 조회
        List<Map<String, Object>> hierarchyData;
        if (upjongCode == null || upjongCode.trim().isEmpty()) {
            hierarchyData = upjongMapper.selectUpjongHierarchy();
        } else {
            hierarchyData = upjongMapper.selectUpjongHierarchyByCode(upjongCode);
        }
        
        // 계층구조 Map 생성
        Map<String, Map<String, List<Map<String, Object>>>> hierarchy = buildHierarchyStructure(hierarchyData);
        
        return Map.of(
            "filterType", filterType,
            "filterCode", upjongCode != null ? upjongCode : "ALL",
            "resultCount", hierarchyData.size(),
            "hierarchy", hierarchy
        );
    }
    
    /**
     * 업종 코드 분석
     */
    private String analyzeUpjongCode(String upjongCode) {
        if (upjongCode == null || upjongCode.trim().isEmpty()) {
            return "ALL";
        }
        
        return switch (upjongCode.length()) {
            case 1 -> "UPJONG1";     // 대분류
            case 3 -> "UPJONG2";     // 중분류  
            case 6 -> "UPJONG3";     // 소분류
            default -> throw new IllegalArgumentException("올바르지 않은 업종 코드 형식입니다: " + upjongCode);
        };
    }
    
    /**
     * 계층구조 데이터 구조화
     */
    private Map<String, Map<String, List<Map<String, Object>>>> buildHierarchyStructure(List<Map<String, Object>> hierarchyData) {
        Map<String, Map<String, List<Map<String, Object>>>> hierarchy = new LinkedHashMap<>();
        
        for (Map<String, Object> row : hierarchyData) {
            String upjong1Nm = (String) row.get("upjong1Nm");
            String upjong2Nm = (String) row.get("upjong2Nm");
            String upjong3Cd = (String) row.get("upjong3Cd");
            String upjong3Nm = (String) row.get("upjong3Nm");
            
            // 대분류 Map 생성 또는 가져오기
            hierarchy.computeIfAbsent(upjong1Nm, k -> new LinkedHashMap<>());
            
            // 중분류 List 생성 또는 가져오기
            hierarchy.get(upjong1Nm).computeIfAbsent(upjong2Nm, k -> new ArrayList<>());
            
            // 소분류 추가
            Map<String, Object> upjong3Info = new HashMap<>();
            upjong3Info.put("code", upjong3Cd);
            upjong3Info.put("name", upjong3Nm);
            
            hierarchy.get(upjong1Nm).get(upjong2Nm).add(upjong3Info);
        }
        
        return hierarchy;
    }

    @Override
    public List<Map<String, Object>> getAnalyzableUpjongList(String admiCd) {
        log.debug("분석 가능한 업종 목록 조회 - 행정동: {}", admiCd);
        
        if (admiCd == null || admiCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 필수입니다.");
        }
        
        List<Map<String, Object>> storeCountList = upjongMapper.selectUpjongStoreCountByAdmi(admiCd);
        
        // 분석 가능한 업종만 필터링 (가맹점 수 >= 3)
        return storeCountList.stream()
                .filter(upjong -> "Y".equals(upjong.get("analyzable")))
                .toList();
    }

    @Override
    public Map<String, Object> getUpjongStoreCount(String admiCd) {
        log.debug("행정동별 업종별 가맹점 수 조회 - 행정동: {}", admiCd);
        
        if (admiCd == null || admiCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 필수입니다.");
        }
        
        List<Map<String, Object>> storeCountList = upjongMapper.selectUpjongStoreCountByAdmi(admiCd);
        
        // 분석 가능한 업종 수 계산
        long analyzableCount = storeCountList.stream()
                .filter(item -> "Y".equals(item.get("analyzable")))
                .count();
        
        return Map.of(
            "admiCd", admiCd,
            "upjongList", storeCountList,
            "totalUpjongCount", storeCountList.size(),
            "analyzableUpjongCount", analyzableCount
        );
    }

    @Override
    public boolean isAnalyzableUpjong(String admiCd, String upjong3Cd) {
        log.debug("업종 분석 가능 여부 체크 - 행정동: {}, 업종: {}", admiCd, upjong3Cd);
        
        if (admiCd == null || admiCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 필수입니다.");
        }
        if (upjong3Cd == null || upjong3Cd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "업종 코드는 필수입니다.");
        }
        
        List<Map<String, Object>> storeCountList = upjongMapper.selectUpjongStoreCountByAdmi(admiCd);
        
        // 해당 업종의 분석 가능 여부 확인
        return storeCountList.stream()
                .filter(upjong -> upjong3Cd.equals(upjong.get("upjong3Cd")))
                .findFirst()
                .map(upjong -> "Y".equals(upjong.get("analyzable")))
                .orElse(false);
    }
    
    @Override
    public Map<String, Object> searchUpjongByName(String upjong3Nm) {
        log.debug("업종명 검색 - 검색어: {}", upjong3Nm);
        
        if (upjong3Nm == null || upjong3Nm.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "검색어를 입력해주세요.");
        }
        
        String trimmedKeyword = upjong3Nm.trim();
        
        // 1. 검색된 업종들의 중분류 코드 목록 조회
        List<Map<String, Object>> searchResults = upjongMapper.selectUpjongByName(trimmedKeyword);
        
        if (searchResults.isEmpty()) {
            return Map.of(
                "keyword", trimmedKeyword,
                "totalCount", 0,
                "hierarchy", Map.of()
            );
        }
        
        // 2. 중분류 코드 목록 추출
        List<String> upjong2Cds = searchResults.stream()
                .map(row -> (String) row.get("upjong2Cd"))
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 해당 중분류들의 전체 계층구조 조회
        List<Map<String, Object>> hierarchyData = upjongMapper.selectUpjongHierarchyByCodes(upjong2Cds);
        
        // 4. 계층구조 생성
        Map<String, Object> hierarchy = buildSearchHierarchy(hierarchyData, trimmedKeyword);
        
        return Map.of(
            "keyword", trimmedKeyword,
            "totalCount", hierarchyData.size(),
            "matchedCount", searchResults.size(),
            "hierarchy", hierarchy
        );
    }
    
    /**
     * 검색 결과용 계층구조 생성
     */
    private Map<String, Object> buildSearchHierarchy(List<Map<String, Object>> data, String keyword) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        data.forEach(row -> {
            String upjong1Cd = (String) row.get("upjong1Cd");
            String upjong1Nm = (String) row.get("upjong1Nm");
            String upjong2Cd = (String) row.get("upjong2Cd");
            String upjong2Nm = (String) row.get("upjong2Nm");
            String upjong3Cd = (String) row.get("upjong3Cd");
            String upjong3Nm = (String) row.get("upjong3Nm");
            
            // 대분류 생성
            @SuppressWarnings("unchecked")
            Map<String, Object> upjong1 = (Map<String, Object>) result.computeIfAbsent(upjong1Cd, k -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("code", upjong1Cd);
                map.put("name", upjong1Nm);
                map.put("children", new LinkedHashMap<String, Object>());
                return map;
            });
            
            // 중분류 생성
            @SuppressWarnings("unchecked")
            Map<String, Object> upjong1Children = (Map<String, Object>) upjong1.get("children");
            @SuppressWarnings("unchecked")
            Map<String, Object> upjong2 = (Map<String, Object>) upjong1Children.computeIfAbsent(upjong2Cd, k -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("code", upjong2Cd);
                map.put("name", upjong2Nm);
                map.put("children", new ArrayList<Map<String, Object>>());
                return map;
            });
            
            // 소분류 추가
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> upjong2Children = (List<Map<String, Object>>) upjong2.get("children");
            Map<String, Object> upjong3 = new LinkedHashMap<>();
            upjong3.put("code", upjong3Cd);
            upjong3.put("name", upjong3Nm);
            upjong3.put("highlighted", upjong3Nm.contains(keyword)); // 검색어 포함 여부
            
            upjong2Children.add(upjong3);
        });
        
        return result;
    }
    
    @Override
    public Map<String, Object> searchUpjongByNameWithStore(String keyword, String admiCd) {
        log.info("[UPJONG-V1] 확장 점포수 기반 업종명 검색 실행 - 검색어: {}, 행정동: {}", keyword, admiCd);
        
        try {
            // 입력값 검증
            validateSearchKeyword(keyword);
            validateAdmiCode(admiCd);
            
            String trimmedKeyword = keyword.trim();
            
            // 빈 검색어 처리
            if (trimmedKeyword.isEmpty()) {
                return Map.of(
                    "keyword", "",
                    "totalCount", 0,
                    "matchedCount", 0,
                    "hierarchy", Map.of()
                );
            }

            // 1단계: 키워드로 일치하는 업종들 조회
            List<Map<String, Object>> searchRows = upjongMapper.selectUpjongByName(trimmedKeyword);
            if (searchRows == null || searchRows.isEmpty()) {
                return Map.of(
                    "keyword", trimmedKeyword,
                    "totalCount", 0,
                    "matchedCount", 0,
                    "hierarchy", Map.of()
                );
            }

            // 2단계: 검색된 업종들의 중분류 코드 수집 (중복 제거)
            List<String> upjong2Cds = new ArrayList<>();
            for (Map<String, Object> row : searchRows) {
                Object cd = row.get("upjong2Cd");
                if (cd instanceof String cdStr && !upjong2Cds.contains(cdStr)) {
                    upjong2Cds.add(cdStr);
                }
            }
            
            log.debug("[UPJONG-V1] 검색된 중분류 코드 수집 완료 - 코드수: {}", upjong2Cds.size());

            // 3단계: 확장 점포수 및 분석가능 여부 포함하여 계층구조 조회
            List<Map<String, Object>> rows = upjongMapper.selectUpjongHierarchyByCodesWithExpandedStore(
                upjong2Cds, admiCd.substring(0, 8));

            // 4단계: DB 결과를 트리 구조로 변환 (점포수, 분석가능 여부, 하이라이트 포함)
            Map<String, Object> hierarchy = buildSearchHierarchyWithStore(rows, trimmedKeyword);

            // 5단계: 최종 응답 데이터 구성
            Map<String, Object> finalResult = Map.of(
                "keyword", trimmedKeyword,
                "totalCount", rows != null ? rows.size() : 0,
                "matchedCount", searchRows.size(),
                "hierarchy", hierarchy
            );
            
            log.info("[UPJONG-V1] 확장 점포수 기반 업종명 검색 완료 - 검색어: {}, 총개수: {}, 매칭개수: {}", 
                     trimmedKeyword, finalResult.get("totalCount"), finalResult.get("matchedCount"));
            return finalResult;
            
        } catch (ValidationException e) {
            log.warn("[UPJONG-V1] 확장 점포수 기반 업종명 검색 검증 실패: keyword={}, admiCd={}, reason={}", 
                     keyword, admiCd, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[UPJONG-V1] 확장 점포수 기반 업종명 검색 처리 오류: keyword={}, admiCd={}", keyword, admiCd, e);
            throw new ValidationException(Status.실패, "업종 검색 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 검색어 검증
     */
    private void validateSearchKeyword(String keyword) throws ValidationException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "검색할 업종명을 입력해주세요.");
        }
        
        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() > 50) {
            throw new ValidationException(Status.파라미터오류, "검색어는 50자 이하로 입력해주세요.");
        }
    }
    
    /**
     * 행정동 코드 검증
     */
    private void validateAdmiCode(String admiCd) throws ValidationException {
        if (admiCd == null || admiCd.trim().length() < 8) {
            throw new ValidationException(Status.파라미터오류, "유효한 행정동 코드(8자리)를 입력해주세요.");
        }
    }
    
    /**
     * 검색 결과용 계층구조 생성 (확장 점포수 포함)
     */
    private Map<String, Object> buildSearchHierarchyWithStore(List<Map<String, Object>> rows, String keyword) {
        Map<String, Object> hierarchy = new LinkedHashMap<>();
        
        for (Map<String, Object> row : rows) {
            // DB 결과에서 업종 정보 추출
            String upjong1Cd = (String) row.get("upjong1Cd");
            String upjong1Nm = (String) row.get("upjong1Nm");
            String upjong2Cd = (String) row.get("upjong2Cd");
            String upjong2Nm = (String) row.get("upjong2Nm");
            String upjong3Cd = (String) row.get("upjong3Cd");
            String upjong3Nm = (String) row.get("upjong3Nm");
            
            // 점포수 및 분석 상태 정보 추출 (타입 안전성 보장)
            Object basicObj = row.get("basicStoreCnt");
            int basicStoreCnt = (basicObj instanceof Number n1) ? n1.intValue() : 0;
            Object expandedObj = row.get("expandedStoreCnt");
            int expandedStoreCnt = (expandedObj instanceof Number n2) ? n2.intValue() : 0;
            Object statusObj = row.get("analysisStatus");
            String analysisStatus = statusObj != null ? String.valueOf(statusObj) : "NOT_ANALYZABLE";

            @SuppressWarnings("unchecked")
            Map<String, Object> upjong1 = (Map<String, Object>) hierarchy.computeIfAbsent(upjong1Cd, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("code", upjong1Cd);
                m.put("name", upjong1Nm);
                m.put("children", new LinkedHashMap<String, Object>());
                return m;
            });

            @SuppressWarnings("unchecked")
            Map<String, Object> upjong1Children = (Map<String, Object>) upjong1.get("children");

            @SuppressWarnings("unchecked")
            Map<String, Object> upjong2 = (Map<String, Object>) upjong1Children.computeIfAbsent(upjong2Cd, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("code", upjong2Cd);
                m.put("name", upjong2Nm);
                m.put("children", new ArrayList<Map<String, Object>>());
                return m;
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> upjong2Children = (List<Map<String, Object>>) upjong2.get("children");

            Map<String, Object> upjong3 = new LinkedHashMap<>();
            upjong3.put("code", upjong3Cd);
            upjong3.put("name", upjong3Nm);
            upjong3.put("highlighted", upjong3Nm != null && !keyword.isEmpty() && upjong3Nm.contains(keyword));
            upjong3.put("basicStoreCnt", basicStoreCnt);
            upjong3.put("expandedStoreCnt", expandedStoreCnt);
            upjong3.put("analysisStatus", analysisStatus);
            upjong2Children.add(upjong3);
        }
        
        return hierarchy;
    }
} 