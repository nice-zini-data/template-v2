package com.zinidata.domain.common.upjong.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.domain.common.upjong.mapper.UpjongMapper;
import com.zinidata.domain.common.upjong.service.UpjongService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 캐시 방식 분석업종 서비스 구현체 💤 현재 미사용
 * 
 * <p>Redis 캐시를 활용하여 업종 정보 조회 성능을 최적화합니다.</p>
 * 
 * <h3>활성화 조건</h3>
 * <ul>
 *   <li>application.yml에 upjong.datasource: redis로 설정한 경우에만 활성화</li>
 *   <li>현재는 설정되지 않아 비활성화 상태</li>
 * </ul>
 * 
 * <h3>특징</h3>
 * <ul>
 *   <li>✅ <strong>장점:</strong> 초고속 응답, DB 부하 대폭 감소, 확장성 우수</li>
 *   <li>❌ <strong>단점:</strong> 구현 복잡도 높음, 캐시 동기화 이슈 가능성, Redis 의존성</li>
 * </ul>
 * 
 * <h3>캐시 전략</h3>
 * <ul>
 *   <li>기본 업종 리스트: 30일 TTL</li>
 *   <li>가맹점 수 정보: 24시간 TTL</li>
 *   <li>Cache-Aside 패턴: Redis 조회 → 없으면 DB 조회 후 캐시 저장</li>
 * </ul>
 * 
 * <p><strong>활성화 방법:</strong> application.yml에 upjong.datasource: redis 추가</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "upjong.datasource", havingValue = "redis")
@RequiredArgsConstructor
public class UpjongServiceRedisImpl implements UpjongService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UpjongMapper upjongMapper;
    private final ObjectMapper objectMapper;
    
    // Redis 키 접두사
    private static final String CACHE_PREFIX = "upjong:";
    private static final String HIERARCHY_KEY = CACHE_PREFIX + "hierarchy";
    private static final String STORE_COUNT_PREFIX = CACHE_PREFIX + "store_count:";
    
    // 캐시 TTL 설정
    private static final long BASIC_CACHE_TTL_DAYS = 30;    // 기본 업종 리스트 (30일)
    private static final long STORE_CACHE_TTL_HOURS = 24;   // 가맹점 수 정보 (24시간)
    


    @Override
    public List<Map<String, Object>> getUpjongList(String level, String upjongCode) {
        log.debug("Redis 캐시 업종 목록 조회 - 레벨: {}, 코드: {}", level, upjongCode);
        
        // 입력값 검증
        if (level == null || level.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "업종 레벨은 필수입니다.");
        }
        
        // 캐시 키 생성 (업종 코드 포함)
        String cacheKey = buildListCacheKey(level, upjongCode);
        
        try {
            // 1. Redis에서 캐시 조회
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                log.debug("Redis 캐시 hit - 키: {}", cacheKey);
                return objectMapper.convertValue(cached, new TypeReference<List<Map<String, Object>>>() {});
            }
            
            // 2. 캐시 miss - DB에서 조회
            log.debug("Redis 캐시 miss - DB 조회 - 키: {}", cacheKey);
            List<Map<String, Object>> upjongList = getFromDatabase(level, upjongCode);
            
            // 3. Redis에 캐시 저장 (기본 업종은 30일 TTL)
            redisTemplate.opsForValue().set(cacheKey, upjongList, BASIC_CACHE_TTL_DAYS, TimeUnit.DAYS);
            log.debug("Redis 캐시 저장 완료 - 키: {}, TTL: {}일", cacheKey, BASIC_CACHE_TTL_DAYS);
            
            return upjongList;
            
        } catch (Exception e) {
            log.error("Redis 캐시 조회 실패 - 키: {}, DB로 fallback", cacheKey, e);
            
            // Redis 실패 시 DB 직접 조회
            return getFromDatabase(level, upjongCode);
        }
    }

    @Override
    public Map<String, Object> getUpjongHierarchy(String upjongCode) {
        log.debug("Redis 캐시 업종 계층구조 조회 - 필터: {}", upjongCode);
        
        // 캐시 키 생성
        String cacheKey = buildHierarchyCacheKey(upjongCode);
        
        try {
            // 1. Redis에서 캐시 조회
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                log.debug("Redis 캐시 hit - 키: {}", cacheKey);
                return objectMapper.convertValue(cached, new TypeReference<Map<String, Object>>() {});
            }
            
            // 2. 캐시 miss - DB에서 조회 및 변환
            log.debug("Redis 캐시 miss - DB 조회 - 키: {}", cacheKey);
            Map<String, Object> hierarchy = buildHierarchyFromDatabase(upjongCode);
            
            // 3. Redis에 캐시 저장 (계층구조도 30일 TTL)
            redisTemplate.opsForValue().set(cacheKey, hierarchy, BASIC_CACHE_TTL_DAYS, TimeUnit.DAYS);
            log.debug("Redis 캐시 저장 완료 - 키: {}, TTL: {}일", cacheKey, BASIC_CACHE_TTL_DAYS);
            
            return hierarchy;
            
        } catch (Exception e) {
            log.error("Redis 캐시 조회 실패 - 키: {}, DB로 fallback", cacheKey, e);
            
            // Redis 실패 시 DB 직접 조회
            return buildHierarchyFromDatabase(upjongCode);
        }
    }
    
    /**
     * 목록 캐시 키 생성
     */
    private String buildListCacheKey(String level, String upjongCode) {
        String baseKey = CACHE_PREFIX + "list:" + level.toLowerCase();
        if (upjongCode != null && !upjongCode.trim().isEmpty()) {
            return baseKey + ":" + upjongCode;
        }
        return baseKey;
    }
    
    /**
     * 데이터베이스에서 업종 목록 조회 (업종 코드 포함)
     */
    private List<Map<String, Object>> getFromDatabase(String level, String upjongCode) {
        return switch (level.toLowerCase()) {
            case "upjong1" -> upjongMapper.selectUpjong1List();  // 대분류는 상위 코드 무시
            case "upjong2" -> upjongMapper.selectUpjong2List(upjongCode);  // 특정 대분류의 중분류들
            case "upjong3" -> upjongMapper.selectUpjong3List(upjongCode);  // 특정 중분류의 소분류들
            default -> throw new IllegalArgumentException("지원하지 않는 업종 레벨입니다: " + level);
        };
    }
    
    /**
     * 계층구조 캐시 키 생성
     */
    private String buildHierarchyCacheKey(String upjongCode) {
        if (upjongCode == null || upjongCode.trim().isEmpty()) {
            return HIERARCHY_KEY;  // "upjong:hierarchy"
        }
        return HIERARCHY_KEY + ":" + upjongCode;  // "upjong:hierarchy:Q" 또는 "upjong:hierarchy:Q13"
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
     * 데이터베이스에서 계층구조 구축 (필터링 가능)
     */
    private Map<String, Object> buildHierarchyFromDatabase(String upjongCode) {
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
     * 계층구조 데이터 구조화
     */
    private Map<String, Map<String, List<Map<String, Object>>>> buildHierarchyStructure(List<Map<String, Object>> hierarchyData) {
        Map<String, Map<String, List<Map<String, Object>>>> hierarchy = new java.util.LinkedHashMap<>();
        
        for (Map<String, Object> row : hierarchyData) {
            String upjong1Nm = (String) row.get("upjong1Nm");
            String upjong2Nm = (String) row.get("upjong2Nm");
            String upjong3Cd = (String) row.get("upjong3Cd");
            String upjong3Nm = (String) row.get("upjong3Nm");
            
            hierarchy.computeIfAbsent(upjong1Nm, k -> new java.util.LinkedHashMap<>());
            hierarchy.get(upjong1Nm).computeIfAbsent(upjong2Nm, k -> new java.util.ArrayList<>());
            
            Map<String, Object> upjong3Info = new java.util.HashMap<>();
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
        
        Map<String, Object> storeCountData = getUpjongStoreCount(admiCd);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> upjongList = (List<Map<String, Object>>) storeCountData.get("upjongList");
        
        // 분석 가능한 업종만 필터링 (가맹점 수 >= 3)
        return upjongList.stream()
                .filter(upjong -> "Y".equals(upjong.get("analyzable")))
                .toList();
    }

    @Override
    public Map<String, Object> getUpjongStoreCount(String admiCd) {
        log.debug("행정동별 업종별 가맹점 수 조회 - 행정동: {}", admiCd);
        
        if (admiCd == null || admiCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 필수입니다.");
        }
        
        String cacheKey = STORE_COUNT_PREFIX + admiCd;
        
        try {
            // 1. Redis에서 캐시 조회
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                log.debug("Redis 캐시 hit - 키: {}", cacheKey);
                return objectMapper.convertValue(cached, new TypeReference<Map<String, Object>>() {});
            }
            
            // 2. 캐시 miss - DB에서 조회
            log.debug("Redis 캐시 miss - DB 조회 - 키: {}", cacheKey);
            List<Map<String, Object>> storeCountList = upjongMapper.selectUpjongStoreCountByAdmi(admiCd);
            
            // 3. 결과 가공
            long analyzableCount = storeCountList.stream()
                    .filter(item -> "Y".equals(item.get("analyzable")))
                    .count();
            
            Map<String, Object> result = Map.of(
                "admiCd", admiCd,
                "upjongList", storeCountList,
                "totalUpjongCount", storeCountList.size(),
                "analyzableUpjongCount", analyzableCount
            );
            
            // 4. Redis에 캐시 저장 (24시간 TTL)
            redisTemplate.opsForValue().set(cacheKey, result, STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Redis 캐시 저장 완료 - 키: {}, TTL: {}시간", cacheKey, STORE_CACHE_TTL_HOURS);
            
            return result;
            
        } catch (Exception e) {
            log.error("Redis 캐시 조회 실패 - 키: {}, DB로 fallback", cacheKey, e);
            
            // Redis 실패 시 DB 직접 조회
            List<Map<String, Object>> storeCountList = upjongMapper.selectUpjongStoreCountByAdmi(admiCd);
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
        
        Map<String, Object> storeCountData = getUpjongStoreCount(admiCd);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> upjongList = (List<Map<String, Object>>) storeCountData.get("upjongList");
        
        // 해당 업종의 분석 가능 여부 확인
        return upjongList.stream()
                .filter(upjong -> upjong3Cd.equals(upjong.get("upjong3Cd")))
                .findFirst()
                .map(upjong -> "Y".equals(upjong.get("analyzable")))
                .orElse(false);
    }
    
    /**
     * 업종 캐시 무효화 (관리자용)
     */
    public void clearUpjongCache() {
        log.info("업종 캐시 무효화 시작");
        
        // 기본 업종 리스트 캐시 삭제
        redisTemplate.delete(CACHE_PREFIX + "list:upjong1");
        redisTemplate.delete(CACHE_PREFIX + "list:upjong2");
        redisTemplate.delete(CACHE_PREFIX + "list:upjong3");
        
        // 모든 계층구조 캐시 삭제 (필터링된 것들 포함)
        Set<String> hierarchyKeys = redisTemplate.keys(HIERARCHY_KEY + "*");
        if (hierarchyKeys != null && !hierarchyKeys.isEmpty()) {
            redisTemplate.delete(hierarchyKeys);
            log.info("계층구조 캐시 삭제 완료 - 삭제된 키 수: {}", hierarchyKeys.size());
        }
        
        log.info("기본 업종 캐시 무효화 완료");
    }
    
    /**
     * 특정 행정동의 가맹점 수 캐시 무효화
     * 
     * @param admiCd 행정동 코드
     */
    public void clearStoreCountCache(String admiCd) {
        if (admiCd == null || admiCd.trim().isEmpty()) {
            log.warn("행정동 코드가 없어 가맹점 수 캐시 무효화를 건너뜁니다.");
            return;
        }
        
        String cacheKey = STORE_COUNT_PREFIX + admiCd;
        redisTemplate.delete(cacheKey);
        log.info("가맹점 수 캐시 무효화 완료 - 행정동: {}", admiCd);
    }
    
    /**
     * 모든 가맹점 수 캐시 무효화 (관리자용)
     */
    public void clearAllStoreCountCache() {
        log.info("모든 가맹점 수 캐시 무효화 시작");
        
        // 패턴 매칭으로 모든 store_count 캐시 삭제
        Set<String> keys = redisTemplate.keys(STORE_COUNT_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("모든 가맹점 수 캐시 무효화 완료 - 삭제된 키 수: {}", keys.size());
        } else {
            log.info("삭제할 가맹점 수 캐시가 없습니다.");
        }
    }
    
    @Override
    public Map<String, Object> searchUpjongByName(String upjong3Nm) {
        log.debug("업종명 검색 - 검색어: {}", upjong3Nm);
        
        // 검색 기능은 실시간 데이터이므로 캐시를 사용하지 않고 직접 DB 조회
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
        log.info("[UPJONG-V1] Redis 확장 점포수 기반 업종명 검색 - 검색어: {}, 행정동: {}", keyword, admiCd);
        
        // Redis 캐시를 사용하지 않고 데이터베이스 서비스에 위임
        // 확장 점포수 기반 검색은 실시간 데이터이므로 캐시 사용 안 함
        UpjongServiceImpl databaseService = new UpjongServiceImpl(upjongMapper);
        return databaseService.searchUpjongByNameWithStore(keyword, admiCd);
    }
} 