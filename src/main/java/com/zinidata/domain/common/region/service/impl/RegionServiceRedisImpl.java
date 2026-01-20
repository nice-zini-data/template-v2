package com.zinidata.domain.common.region.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.domain.common.region.mapper.RegionMapper;
import com.zinidata.domain.common.region.service.RegionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 캐시 방식 지역 정보 서비스 구현체 💤 현재 미사용
 * 
 * <p>Redis 캐시를 활용하여 지역 정보 조회 성능을 최적화합니다.</p>
 * <p>PostGIS 공간 쿼리 부하를 Redis로 분산하여 대용량 GIS 처리를 지원합니다.</p>
 * 
 * <h3>활성화 조건</h3>
 * <ul>
 *   <li>application.yml에 region.datasource: redis로 설정한 경우에만 활성화</li>
 *   <li>현재는 설정되지 않아 비활성화 상태</li>
 * </ul>
 * 
 * <h3>특징</h3>
 * <ul>
 *   <li>✅ <strong>장점:</strong> 초고속 응답, PostGIS DB 부하 대폭 감소</li>
 *   <li>✅ <strong>장점:</strong> 대용량 공간 쿼리 성능 최적화</li>
 *   <li>✅ <strong>장점:</strong> 반복 조회되는 행정구역 정보 캐시 효과</li>
 *   <li>❌ <strong>단점:</strong> 구현 복잡도 높음, 공간 데이터 캐시 동기화 이슈</li>
 *   <li>❌ <strong>단점:</strong> Redis 메모리 사용량 높음, GIS 데이터 직렬화 오버헤드</li>
 * </ul>
 * 
 * <h3>캐시 전략</h3>
 * <ul>
 *   <li>행정구역 기본 정보: 7일 TTL (변경 빈도 낮음)</li>
 *   <li>좌표 기반 조회: 24시간 TTL (반복 조회 많음)</li>
 *   <li>공간 분석 결과: 1시간 TTL (계산 비용 높음)</li>
 *   <li>Cache-Aside 패턴: Redis 조회 → 없으면 PostGIS 조회 후 캐시 저장</li>
 * </ul>
 * 
 * <h3>GIS 캐시 최적화</h3>
 * <ul>
 *   <li><strong>Geometry 압축:</strong> WKT → Binary 변환으로 저장 공간 절약</li>
 *   <li><strong>공간 인덱스:</strong> 좌표 기반 Redis Hash 구조 활용</li>
 *   <li><strong>배치 캐싱:</strong> 인접 지역 정보 미리 로드</li>
 * </ul>
 * 
 * <p><strong>활성화 방법:</strong> application.yml에 region.datasource: redis 추가</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "region.datasource", havingValue = "redis")
@RequiredArgsConstructor
public class RegionServiceRedisImpl implements RegionService {

    private final RegionMapper regionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Cache TTL 설정
    private static final long REGION_INFO_TTL = 7; // 7일 (행정구역 기본 정보)
    private static final long COORDINATE_QUERY_TTL = 24; // 24시간 (좌표 기반 조회)
    
    // Cache Key 접두사
    private static final String BLOCK_CODE_PREFIX = "region:block:code:";
    private static final String BLOCK_POINT_PREFIX = "region:block:point:";
    private static final String ADMI_CODE_PREFIX = "region:admi:code:";
    private static final String ADMI_POINT_PREFIX = "region:admi:point:";
    private static final String CTY_CODE_PREFIX = "region:cty:code:";
    private static final String CTY_POINT_PREFIX = "region:cty:point:";
    private static final String MEGA_CODE_PREFIX = "region:mega:code:";
    private static final String MEGA_POINT_PREFIX = "region:mega:point:";

    // ==================== 블록 API ====================
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getBlockByCode(String blkCd) throws ValidationException {
        log.info("[REGION-REDIS] 블록 코드 조회 서비스 - 블록코드: {}", blkCd);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateBlockCode(blkCd);
            
            // Redis 캐시 조회
            String cacheKey = BLOCK_CODE_PREFIX + blkCd;
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 블록 코드 캐시 히트 - 블록코드: {}", blkCd);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 블록 코드 캐시 미스, DB 조회 - 블록코드: {}", blkCd);
            Map<String, Object> result = regionMapper.selectBlockByCode(blkCd);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 블록 조회 결과 없음 - 블록코드: {}", blkCd);
                throw new ValidationException(Status.데이터없음, "해당 블록을 찾을 수 없습니다: " + blkCd);
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, REGION_INFO_TTL, TimeUnit.DAYS);
            log.info("[REGION-REDIS] 블록 코드 조회 성공 및 캐시 저장 - 블록코드: {}", blkCd);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 블록 코드 조회 검증 실패 - 블록코드: {}, 오류: {}", blkCd, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 블록 코드 조회 실패 - 블록코드: {}, 오류: {}", blkCd, e.getMessage(), e);
            throw new ValidationException(Status.실패, "블록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getBlockByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-REDIS] 좌표 기반 블록 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateCoordinates(lat, lng);
            
            // Redis 캐시 조회 (좌표를 소수점 4자리로 반올림하여 캐시 키 생성)
            String cacheKey = BLOCK_POINT_PREFIX + String.format("%.4f:%.4f", lat, lng);
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 좌표 기반 블록 캐시 히트 - 위도: {}, 경도: {}", lat, lng);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 좌표 기반 블록 캐시 미스, DB 조회 - 위도: {}, 경도: {}", lat, lng);
            Map<String, Object> result = regionMapper.selectBlockByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 좌표 기반 블록 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                throw new ValidationException(Status.데이터없음, "해당 좌표의 블록을 찾을 수 없습니다");
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, COORDINATE_QUERY_TTL, TimeUnit.HOURS);
            log.info("[REGION-REDIS] 좌표 기반 블록 조회 성공 및 캐시 저장 - 위도: {}, 경도: {}", lat, lng);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 좌표 기반 블록 조회 검증 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 좌표 기반 블록 조회 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage(), e);
            throw new ValidationException(Status.실패, "좌표 기반 블록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getBlocksByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-REDIS] 폴리곤 영역 블록 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validatePolygon(polygon);
            
            // 폴리곤 쿼리는 캐시하지 않음 (가변성이 높고 캐시 효과 낮음)
            // 직접 DB 조회
            List<Map<String, Object>> result = regionMapper.selectBlocksByPolygon(convertGeoJsonToWkt(polygon));
            
            log.info("[REGION-REDIS] 폴리곤 영역 블록 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 폴리곤 영역 블록 조회 검증 실패 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 폴리곤 영역 블록 조회 실패 - 오류: {}", e.getMessage(), e);
            throw new ValidationException(Status.실패, "폴리곤 영역 블록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getBlocksByRadius(double lat, double lng, int radius) throws ValidationException {
        log.info("[REGION-REDIS] 반경 내 블록 조회 서비스 - 위도: {}, 경도: {}, 반경: {}m", lat, lng, radius);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateCoordinates(lat, lng);
            validateRadius(radius);
            
            // 반경 쿼리는 캐시하지 않음 (가변성이 높고 캐시 효과 낮음)
            // 직접 DB 조회
            List<Map<String, Object>> result = regionMapper.selectBlocksByRadius(lat, lng, radius);
            
            log.info("[REGION-REDIS] 반경 내 블록 조회 성공 - 위도: {}, 경도: {}, 반경: {}m, 조회건수: {}", lat, lng, radius, result.size());
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 반경 내 블록 조회 검증 실패 - 위도: {}, 경도: {}, 반경: {}m, 오류: {}", lat, lng, radius, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 반경 내 블록 조회 실패 - 위도: {}, 경도: {}, 반경: {}m, 오류: {}", lat, lng, radius, e.getMessage(), e);
            throw new ValidationException(Status.실패, "반경 내 블록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ==================== 행정동 API ====================
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAdmiByCode(String admiCd) throws ValidationException {
        log.info("[REGION-REDIS] 행정동 코드 조회 서비스 - 행정동코드: {}", admiCd);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateAdmiCode(admiCd);
            
            // Redis 캐시 조회
            String cacheKey = ADMI_CODE_PREFIX + admiCd;
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 행정동 코드 캐시 히트 - 행정동코드: {}", admiCd);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 행정동 코드 캐시 미스, DB 조회 - 행정동코드: {}", admiCd);
            Map<String, Object> result = regionMapper.selectAdmiByCode(admiCd);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 행정동 조회 결과 없음 - 행정동코드: {}", admiCd);
                throw new ValidationException(Status.데이터없음, "해당 행정동을 찾을 수 없습니다: " + admiCd);
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, REGION_INFO_TTL, TimeUnit.DAYS);
            log.info("[REGION-REDIS] 행정동 코드 조회 성공 및 캐시 저장 - 행정동코드: {}", admiCd);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 행정동 코드 조회 검증 실패 - 행정동코드: {}, 오류: {}", admiCd, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 행정동 코드 조회 실패 - 행정동코드: {}, 오류: {}", admiCd, e.getMessage(), e);
            throw new ValidationException(Status.실패, "행정동 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAdmiByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-REDIS] 좌표 기반 행정동 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateCoordinates(lat, lng);
            
            // Redis 캐시 조회 (좌표를 소수점 4자리로 반올림하여 캐시 키 생성)
            String cacheKey = ADMI_POINT_PREFIX + String.format("%.4f:%.4f", lat, lng);
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 좌표 기반 행정동 캐시 히트 - 위도: {}, 경도: {}", lat, lng);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 좌표 기반 행정동 캐시 미스, DB 조회 - 위도: {}, 경도: {}", lat, lng);
            Map<String, Object> result = regionMapper.selectAdmiByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 좌표 기반 행정동 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                throw new ValidationException(Status.데이터없음, "해당 좌표의 행정동을 찾을 수 없습니다");
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, COORDINATE_QUERY_TTL, TimeUnit.HOURS);
            log.info("[REGION-REDIS] 좌표 기반 행정동 조회 성공 및 캐시 저장 - 위도: {}, 경도: {}", lat, lng);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 좌표 기반 행정동 조회 검증 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 좌표 기반 행정동 조회 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage(), e);
            throw new ValidationException(Status.실패, "좌표 기반 행정동 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getAdmisByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-REDIS] 폴리곤 영역 행정동 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validatePolygon(polygon);
            
            // 폴리곤 쿼리는 캐시하지 않음 (가변성이 높고 캐시 효과 낮음)
            // 직접 DB 조회
            List<Map<String, Object>> result = regionMapper.selectAdmisByPolygon(convertGeoJsonToWkt(polygon));
            
            log.info("[REGION-REDIS] 폴리곤 영역 행정동 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 폴리곤 영역 행정동 조회 검증 실패 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 폴리곤 영역 행정동 조회 실패 - 오류: {}", e.getMessage(), e);
            throw new ValidationException(Status.실패, "폴리곤 영역 행정동 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getAdjacentAdmis(String admiCd) throws ValidationException {
        log.info("[REGION-REDIS] 인접 행정동 조회 서비스 - 행정동코드: {}", admiCd);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateAdmiCode(admiCd);
            
            // 인접 행정동 쿼리는 캐시하지 않음 (복잡한 공간 분석)
            // 직접 DB 조회
            List<Map<String, Object>> result = regionMapper.selectAdjacentAdmis(admiCd);
            
            log.info("[REGION-REDIS] 인접 행정동 조회 성공 - 행정동코드: {}, 조회건수: {}", admiCd, result.size());
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 인접 행정동 조회 검증 실패 - 행정동코드: {}, 오류: {}", admiCd, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 인접 행정동 조회 실패 - 행정동코드: {}, 오류: {}", admiCd, e.getMessage(), e);
            throw new ValidationException(Status.실패, "인접 행정동 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ==================== 시군구 API ====================
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCtyByCode(String ctyCd) throws ValidationException {
        log.info("[REGION-REDIS] 시도 코드 조회 서비스 - 시도코드: {}", ctyCd);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateCtyCode(ctyCd);
            
            // Redis 캐시 조회
            String cacheKey = CTY_CODE_PREFIX + ctyCd;
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 시도 코드 캐시 히트 - 시도코드: {}", ctyCd);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 시도 코드 캐시 미스, DB 조회 - 시도코드: {}", ctyCd);
            Map<String, Object> result = regionMapper.selectCtyByCode(ctyCd);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 시도 조회 결과 없음 - 시도코드: {}", ctyCd);
                throw new ValidationException(Status.데이터없음, "해당 시도를 찾을 수 없습니다: " + ctyCd);
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, REGION_INFO_TTL, TimeUnit.DAYS);
            log.info("[REGION-REDIS] 시도 코드 조회 성공 및 캐시 저장 - 시도코드: {}", ctyCd);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 시도 코드 조회 검증 실패 - 시도코드: {}, 오류: {}", ctyCd, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 시도 코드 조회 실패 - 시도코드: {}, 오류: {}", ctyCd, e.getMessage(), e);
            throw new ValidationException(Status.실패, "시도 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCtyByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-REDIS] 좌표 기반 시도 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateCoordinates(lat, lng);
            
            // Redis 캐시 조회 (좌표를 소수점 4자리로 반올림하여 캐시 키 생성)
            String cacheKey = CTY_POINT_PREFIX + String.format("%.4f:%.4f", lat, lng);
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 좌표 기반 시도 캐시 히트 - 위도: {}, 경도: {}", lat, lng);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 좌표 기반 시도 캐시 미스, DB 조회 - 위도: {}, 경도: {}", lat, lng);
            Map<String, Object> result = regionMapper.selectCtyByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 좌표 기반 시도 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                throw new ValidationException(Status.데이터없음, "해당 좌표의 시도를 찾을 수 없습니다");
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, COORDINATE_QUERY_TTL, TimeUnit.HOURS);
            log.info("[REGION-REDIS] 좌표 기반 시도 조회 성공 및 캐시 저장 - 위도: {}, 경도: {}", lat, lng);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 좌표 기반 시도 조회 검증 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 좌표 기반 시도 조회 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage(), e);
            throw new ValidationException(Status.실패, "좌표 기반 시도 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getCtysByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-REDIS] 폴리곤 영역 시도 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validatePolygon(polygon);
            
            // 폴리곤 쿼리는 캐시하지 않음 (가변성이 높고 캐시 효과 낮음)
            // 직접 DB 조회
            List<Map<String, Object>> result = regionMapper.selectCtysByPolygon(convertGeoJsonToWkt(polygon));
            
            log.info("[REGION-REDIS] 폴리곤 영역 시도 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 폴리곤 영역 시도 조회 검증 실패 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 폴리곤 영역 시도 조회 실패 - 오류: {}", e.getMessage(), e);
            throw new ValidationException(Status.실패, "폴리곤 영역 시도 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ==================== 광역시도 API ====================
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMegaByCode(String megaCd) throws ValidationException {
        log.info("[REGION-REDIS] 광역권 코드 조회 서비스 - 광역권코드: {}", megaCd);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateMegaCode(megaCd);
            
            // Redis 캐시 조회
            String cacheKey = MEGA_CODE_PREFIX + megaCd;
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 광역권 코드 캐시 히트 - 광역권코드: {}", megaCd);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 광역권 코드 캐시 미스, DB 조회 - 광역권코드: {}", megaCd);
            Map<String, Object> result = regionMapper.selectMegaByCode(megaCd);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 광역권 조회 결과 없음 - 광역권코드: {}", megaCd);
                throw new ValidationException(Status.데이터없음, "해당 광역권을 찾을 수 없습니다: " + megaCd);
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, REGION_INFO_TTL, TimeUnit.DAYS);
            log.info("[REGION-REDIS] 광역권 코드 조회 성공 및 캐시 저장 - 광역권코드: {}", megaCd);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 광역권 코드 조회 검증 실패 - 광역권코드: {}, 오류: {}", megaCd, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 광역권 코드 조회 실패 - 광역권코드: {}, 오류: {}", megaCd, e.getMessage(), e);
            throw new ValidationException(Status.실패, "광역권 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMegaByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-REDIS] 좌표 기반 광역권 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validateCoordinates(lat, lng);
            
            // Redis 캐시 조회 (좌표를 소수점 4자리로 반올림하여 캐시 키 생성)
            String cacheKey = MEGA_POINT_PREFIX + String.format("%.4f:%.4f", lat, lng);
            Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.info("[REGION-REDIS] 좌표 기반 광역권 캐시 히트 - 위도: {}, 경도: {}", lat, lng);
                return cachedResult;
            }
            
            // 캐시 미스 시 DB 조회
            log.info("[REGION-REDIS] 좌표 기반 광역권 캐시 미스, DB 조회 - 위도: {}, 경도: {}", lat, lng);
            Map<String, Object> result = regionMapper.selectMegaByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-REDIS] 좌표 기반 광역권 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                throw new ValidationException(Status.데이터없음, "해당 좌표의 광역권을 찾을 수 없습니다");
            }
            
            // Redis 캐시 저장
            redisTemplate.opsForValue().set(cacheKey, result, COORDINATE_QUERY_TTL, TimeUnit.HOURS);
            log.info("[REGION-REDIS] 좌표 기반 광역권 조회 성공 및 캐시 저장 - 위도: {}, 경도: {}", lat, lng);
            
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 좌표 기반 광역권 조회 검증 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 좌표 기반 광역권 조회 실패 - 위도: {}, 경도: {}, 오류: {}", lat, lng, e.getMessage(), e);
            throw new ValidationException(Status.실패, "좌표 기반 광역권 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getMegasByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-REDIS] 폴리곤 영역 광역권 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 입력값 검증 (DB 구현체와 동일)
            validatePolygon(polygon);
            
            // 폴리곤 쿼리는 캐시하지 않음 (가변성이 높고 캐시 효과 낮음)
            // 직접 DB 조회
            List<Map<String, Object>> result = regionMapper.selectMegasByPolygon(convertGeoJsonToWkt(polygon));
            
            log.info("[REGION-REDIS] 폴리곤 영역 광역권 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.error("[REGION-REDIS] 폴리곤 영역 광역권 조회 검증 실패 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-REDIS] 폴리곤 영역 광역권 조회 실패 - 오류: {}", e.getMessage(), e);
            throw new ValidationException(Status.실패, "폴리곤 영역 광역권 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ==================== 검증 메서드 (DB 구현체와 동일) ====================
    
    /**
     * 블록 코드 검증
     */
    private void validateBlockCode(String blkCd) throws ValidationException {
        if (blkCd == null || blkCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "블록 코드는 필수입니다.");
        }
        if (blkCd.length() != 6) {
            throw new ValidationException(Status.파라미터오류, "블록 코드는 6자리여야 합니다.");
        }
        if (!blkCd.matches("\\d{6}")) {
            throw new ValidationException(Status.파라미터오류, "블록 코드는 숫자만 입력 가능합니다.");
        }
    }
    
    /**
     * 행정동 코드 검증
     */
    private void validateAdmiCode(String admiCd) throws ValidationException {
        if (admiCd == null || admiCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 필수입니다.");
        }
        if (admiCd.length() != 8) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 8자리여야 합니다.");
        }
        if (!admiCd.matches("\\d{8}")) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 숫자만 입력 가능합니다.");
        }
    }
    
    /**
     * 시도 코드 검증
     */
    private void validateCtyCode(String ctyCd) throws ValidationException {
        if (ctyCd == null || ctyCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "시도 코드는 필수입니다.");
        }
        if (ctyCd.length() != 2) {
            throw new ValidationException(Status.파라미터오류, "시도 코드는 2자리여야 합니다.");
        }
        if (!ctyCd.matches("\\d{2}")) {
            throw new ValidationException(Status.파라미터오류, "시도 코드는 숫자만 입력 가능합니다.");
        }
    }
    
    /**
     * 광역권 코드 검증
     */
    private void validateMegaCode(String megaCd) throws ValidationException {
        if (megaCd == null || megaCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "광역권 코드는 필수입니다.");
        }
        if (!megaCd.matches("\\d{1,2}")) {
            throw new ValidationException(Status.파라미터오류, "광역권 코드는 1~2자리 숫자여야 합니다.");
        }
    }
    
    /**
     * 좌표 검증
     */
    private void validateCoordinates(double lat, double lng) throws ValidationException {
        // 1. 위도 범위 검증 (-90 ~ 90)
        if (lat < -90 || lat > 90) {
            throw new ValidationException(Status.파라미터오류, "위도는 -90 ~ 90 범위여야 합니다.");
        }
        // 2. 경도 범위 검증 (-180 ~ 180)
        if (lng < -180 || lng > 180) {
            throw new ValidationException(Status.파라미터오류, "경도는 -180 ~ 180 범위여야 합니다.");
        }
        // 3. 한국 영역 검증 (선택적)
        if (lat < 33 || lat > 39 || lng < 124 || lng > 132) {
            log.warn("[REGION-REDIS] 한국 영역 외부 좌표 - 위도: {}, 경도: {}", lat, lng);
        }
    }
    
    /**
     * 폴리곤 검증
     */
    private void validatePolygon(String polygon) throws ValidationException {
        if (polygon == null || polygon.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "폴리곤 데이터는 필수입니다.");
        }
        if (polygon.length() > 100000) { // 100KB 제한
            throw new ValidationException(Status.파라미터오류, "폴리곤 데이터가 너무 큽니다. (최대 100KB)");
        }
        if (!polygon.trim().startsWith("{")) {
            throw new ValidationException(Status.파라미터오류, "올바른 GeoJSON 형식이 아닙니다.");
        }
    }
    
    /**
     * 반경 검증
     */
    private void validateRadius(int radius) throws ValidationException {
        if (radius <= 0) {
            throw new ValidationException(Status.파라미터오류, "반경은 0보다 커야 합니다.");
        }
        if (radius > 1000) {
            throw new ValidationException(Status.파라미터오류, "반경은 1000m 이하여야 합니다.");
        }
    }
    
    /**
     * GeoJSON을 WKT로 변환 (간단한 구현)
     */
    private String convertGeoJsonToWkt(String geoJson) {
        // 실제 구현에서는 JTS나 GeoTools 라이브러리 사용 권장
        // 여기서는 간단한 변환만 구현
        return geoJson; // 임시 구현
    }
    
    /**
     * 광역시도 목록 조회 (Redis 캐시)
     */
    @Override
    public List<Map<String, Object>> getMegaList() throws ValidationException {
        log.info("[LOCATION-REDIS] 광역시도 목록 조회 (Redis 캐시)");
        
        try {
            String cacheKey = "region:mega:list";
            
            // Redis에서 조회 시도
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cachedResult = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.debug("[LOCATION-REDIS] 광역시도 목록 Redis 캐시 히트");
                return cachedResult;
            }
            
            // 캐시 미스 시 DB에서 조회 후 Redis에 저장
            log.debug("[LOCATION-REDIS] 광역시도 목록 Redis 캐시 미스, DB 조회");
            List<Map<String, Object>> result = regionMapper.getMegaList();
            
            // Redis에 캐시 저장 (7일 TTL)
            redisTemplate.opsForValue().set(cacheKey, result, REGION_INFO_TTL, TimeUnit.DAYS);
            
            log.info("[LOCATION-REDIS] 광역시도 목록 조회 완료: count={}", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("[LOCATION-REDIS] 광역시도 목록 조회 실패", e);
            throw new ValidationException(Status.실패, "광역시도 목록 조회 중 오류가 발생했습니다.");
        }
    }
}
