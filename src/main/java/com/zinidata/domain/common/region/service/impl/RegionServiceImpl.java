package com.zinidata.domain.common.region.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.domain.common.region.mapper.RegionMapper;
import com.zinidata.domain.common.region.service.RegionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 데이터베이스 방식 지역 정보 서비스 구현체 ⭐ 현재 사용 중
 * 
 * <p>PostGIS를 사용하여 데이터베이스에서 지역 정보를 조회합니다.</p>
 * <p>블록, 행정동, 시도, 광역권 관련 비즈니스 로직을 처리합니다.</p>
 * 
 * <h3>활성화 조건</h3>
 * <ul>
 *   <li>application.yml에 region.datasource 설정이 없는 경우 (기본값)</li>
 *   <li>region.datasource: database로 명시적 설정한 경우</li>
 * </ul>
 * 
 * <h3>특징</h3>
 * <ul>
 *   <li>✅ <strong>장점:</strong> PostGIS 공간 쿼리 직접 활용, 정확한 GIS 연산</li>
 *   <li>✅ <strong>장점:</strong> 복잡한 공간 분석 쿼리 지원 (ST_Contains, ST_DWithin 등)</li>
 *   <li>✅ <strong>장점:</strong> 실시간 데이터 정확성 보장</li>
 *   <li>❌ <strong>단점:</strong> DB 부하 높음, 대용량 공간 쿼리 시 성능 이슈 가능</li>
 * </ul>
 * 
 * <h3>GIS 기능</h3>
 * <ul>
 *   <li><strong>Point in Polygon:</strong> 좌표 기반 행정구역 조회</li>
 *   <li><strong>Spatial Join:</strong> 폴리곤 영역 내 지역 목록 조회</li>
 *   <li><strong>Buffer Query:</strong> 반경 내 지역 검색</li>
 *   <li><strong>Geometry 변환:</strong> GeoJSON ↔ WKT 포맷 처리</li>
 * </ul>
 * 
 * <p><strong>Redis 구현체로 변경하려면:</strong> application.yml에 region.datasource: redis 추가</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "region.datasource", havingValue = "database", matchIfMissing = true)
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionMapper regionMapper;

    // ==================== 블록 API ====================
    
    @Override
    public Map<String, Object> getBlockByCode(String blkCd) throws ValidationException {
        log.info("[REGION-V1] 블록 코드 조회 서비스 - 블록코드: {}", blkCd);
        
        try {
            // 입력값 검증
            validateBlockCode(blkCd);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectBlockByCode(blkCd);
            
            if (result == null) {
                log.warn("[REGION-V1] 블록 조회 결과 없음 - 블록코드: {}", blkCd);
                throw new ValidationException(Status.데이터없음, "해당 블록을 찾을 수 없습니다: " + blkCd);
            }
            
            log.info("[REGION-V1] 블록 코드 조회 성공 - 블록코드: {}", blkCd);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 블록 코드 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 블록 코드 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "블록 코드 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public Map<String, Object> getBlockByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-V1] 좌표 기반 블록 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 좌표 검증
            validateCoordinates(lat, lng);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectBlockByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-V1] 좌표 기반 블록 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                return null; // 좌표에 해당하는 블록이 없을 수 있음
            }
            
            log.info("[REGION-V1] 좌표 기반 블록 조회 성공 - 위도: {}, 경도: {}", lat, lng);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 좌표 기반 블록 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 좌표 기반 블록 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "좌표 기반 블록 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public List<Map<String, Object>> getBlocksByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-V1] 폴리곤 영역 블록 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 폴리곤 검증
            validatePolygon(polygon);
            
            // WKT 형식 변환
            String wktPolygon = convertToWKT(polygon);
            
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.selectBlocksByPolygon(wktPolygon);
            
            log.info("[REGION-V1] 폴리곤 영역 블록 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 폴리곤 영역 블록 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 폴리곤 영역 블록 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "폴리곤 영역 블록 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public List<Map<String, Object>> getBlocksByRadius(double lat, double lng, int radius) throws ValidationException {
        log.info("[REGION-V1] 반경 내 블록 조회 서비스 - 위도: {}, 경도: {}, 반경: {}m", lat, lng, radius);
        
        try {
            // 좌표 및 반경 검증
            validateCoordinates(lat, lng);
            validateRadius(radius);
            
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.selectBlocksByRadius(lat, lng, radius);
            
            log.info("[REGION-V1] 반경 내 블록 조회 성공 - 위도: {}, 경도: {}, 반경: {}m, 조회건수: {}", lat, lng, radius, result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 반경 내 블록 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 반경 내 블록 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "반경 내 블록 조회 중 오류가 발생했습니다.");
        }
    }

    // ==================== 행정동 API ====================
    
    @Override
    public Map<String, Object> getAdmiByCode(String admiCd) throws ValidationException {
        log.info("[REGION-V1] 행정동 코드 조회 서비스 - 행정동코드: {}", admiCd);
        
        try {
            // 입력값 검증
            validateAdmiCode(admiCd);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectAdmiByCode(admiCd);
            
            if (result == null) {
                log.warn("[REGION-V1] 행정동 조회 결과 없음 - 행정동코드: {}", admiCd);
                throw new ValidationException(Status.데이터없음, "해당 행정동을 찾을 수 없습니다: " + admiCd);
            }
            
            log.info("[REGION-V1] 행정동 코드 조회 성공 - 행정동코드: {}", admiCd);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 행정동 코드 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 행정동 코드 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "행정동 코드 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public Map<String, Object> getAdmiByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-V1] 좌표 기반 행정동 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 좌표 검증
            validateCoordinates(lat, lng);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectAdmiByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-V1] 좌표 기반 행정동 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                return null; // 좌표에 해당하는 행정동이 없을 수 있음
            }
            
            log.info("[REGION-V1] 좌표 기반 행정동 조회 성공 - 위도: {}, 경도: {}", lat, lng);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 좌표 기반 행정동 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 좌표 기반 행정동 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "좌표 기반 행정동 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public List<Map<String, Object>> getAdmisByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-V1] 폴리곤 영역 행정동 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 폴리곤 검증
            validatePolygon(polygon);
            
            // WKT 형식 변환
            String wktPolygon = convertToWKT(polygon);
            
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.selectAdmisByPolygon(wktPolygon);
            
            log.info("[REGION-V1] 폴리곤 영역 행정동 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 폴리곤 영역 행정동 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 폴리곤 영역 행정동 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "폴리곤 영역 행정동 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public List<Map<String, Object>> getAdjacentAdmis(String admiCd) throws ValidationException {
        log.info("[REGION-V1] 인접 행정동 조회 서비스 - 기준 행정동: {}", admiCd);
        
        try {
            // 입력값 검증
            validateAdmiCode(admiCd);
            
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.selectAdjacentAdmis(admiCd);
            
            if (result == null || result.isEmpty()) {
                log.warn("[REGION-V1] 인접 행정동 조회 결과 없음 - 기준 행정동: {}", admiCd);
                throw new ValidationException(Status.데이터없음, "해당 행정동을 찾을 수 없습니다: " + admiCd);
            }
            
            log.info("[REGION-V1] 인접 행정동 조회 성공 - 기준 행정동: {}, 조회건수: {}", admiCd, result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 인접 행정동 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 인접 행정동 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "인접 행정동 조회 중 오류가 발생했습니다.");
        }
    }

    // ==================== 시도 API ====================
    
    @Override
    public Map<String, Object> getCtyByCode(String ctyCd) throws ValidationException {
        log.info("[REGION-V1] 시도 코드 조회 서비스 - 시도코드: {}", ctyCd);
        
        try {
            // 입력값 검증
            validateCtyCode(ctyCd);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectCtyByCode(ctyCd);
            
            if (result == null) {
                log.warn("[REGION-V1] 시도 조회 결과 없음 - 시도코드: {}", ctyCd);
                throw new ValidationException(Status.데이터없음, "해당 시도를 찾을 수 없습니다: " + ctyCd);
            }
            
            log.info("[REGION-V1] 시도 코드 조회 성공 - 시도코드: {}", ctyCd);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 시도 코드 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 시도 코드 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "시도 코드 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public Map<String, Object> getCtyByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-V1] 좌표 기반 시도 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 좌표 검증
            validateCoordinates(lat, lng);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectCtyByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-V1] 좌표 기반 시도 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                return null; // 좌표에 해당하는 시도가 없을 수 있음
            }
            
            log.info("[REGION-V1] 좌표 기반 시도 조회 성공 - 위도: {}, 경도: {}", lat, lng);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 좌표 기반 시도 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 좌표 기반 시도 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "좌표 기반 시도 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public List<Map<String, Object>> getCtysByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-V1] 폴리곤 영역 시도 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 폴리곤 검증
            validatePolygon(polygon);
            
            // WKT 형식 변환
            String wktPolygon = convertToWKT(polygon);
            
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.selectCtysByPolygon(wktPolygon);
            
            log.info("[REGION-V1] 폴리곤 영역 시도 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 폴리곤 영역 시도 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 폴리곤 영역 시도 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "폴리곤 영역 시도 조회 중 오류가 발생했습니다.");
        }
    }

    // ==================== 광역권 API ====================
    
    @Override
    public Map<String, Object> getMegaByCode(String megaCd) throws ValidationException {
        log.info("[REGION-V1] 광역권 코드 조회 서비스 - 광역권코드: {}", megaCd);
        
        try {
            // 입력값 검증
            validateMegaCode(megaCd);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectMegaByCode(megaCd);
            
            if (result == null) {
                log.warn("[REGION-V1] 광역권 조회 결과 없음 - 광역권코드: {}", megaCd);
                throw new ValidationException(Status.데이터없음, "해당 광역권을 찾을 수 없습니다: " + megaCd);
            }
            
            log.info("[REGION-V1] 광역권 코드 조회 성공 - 광역권코드: {}", megaCd);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 광역권 코드 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 광역권 코드 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "광역권 코드 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public Map<String, Object> getMegaByPoint(double lat, double lng) throws ValidationException {
        log.info("[REGION-V1] 좌표 기반 광역권 조회 서비스 - 위도: {}, 경도: {}", lat, lng);
        
        try {
            // 좌표 검증
            validateCoordinates(lat, lng);
            
            // 데이터 조회
            Map<String, Object> result = regionMapper.selectMegaByPoint(lat, lng);
            
            if (result == null) {
                log.warn("[REGION-V1] 좌표 기반 광역권 조회 결과 없음 - 위도: {}, 경도: {}", lat, lng);
                return null; // 좌표에 해당하는 광역권이 없을 수 있음
            }
            
            log.info("[REGION-V1] 좌표 기반 광역권 조회 성공 - 위도: {}, 경도: {}", lat, lng);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 좌표 기반 광역권 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 좌표 기반 광역권 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "좌표 기반 광역권 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public List<Map<String, Object>> getMegasByPolygon(String polygon) throws ValidationException {
        log.info("[REGION-V1] 폴리곤 영역 광역권 조회 서비스 - 폴리곤 크기: {}", polygon.length());
        
        try {
            // 폴리곤 검증
            validatePolygon(polygon);
            
            // WKT 형식 변환
            String wktPolygon = convertToWKT(polygon);
            
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.selectMegasByPolygon(wktPolygon);
            
            log.info("[REGION-V1] 폴리곤 영역 광역권 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 폴리곤 영역 광역권 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 폴리곤 영역 광역권 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "폴리곤 영역 광역권 조회 중 오류가 발생했습니다.");
        }
    }

    // ==================== 검증 메서드 ====================
    
    /**
     * 블록 코드 검증
     */
    private void validateBlockCode(String blkCd) throws ValidationException {
        if (blkCd == null || blkCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "블록 코드를 입력해주세요.");
        }
        
        if (!blkCd.matches("\\d{6}")) {
            throw new ValidationException(Status.파라미터오류, "블록 코드는 6자리 숫자여야 합니다.");
        }
    }
    
    /**
     * 행정동 코드 검증
     */
    private void validateAdmiCode(String admiCd) throws ValidationException {
        if (admiCd == null || admiCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드를 입력해주세요.");
        }
        
        if (!admiCd.matches("\\d{8}")) {
            throw new ValidationException(Status.파라미터오류, "행정동 코드는 8자리 숫자여야 합니다.");
        }
    }
    
    /**
     * 시도 코드 검증
     */
    private void validateCtyCode(String ctyCd) throws ValidationException {
        if (ctyCd == null || ctyCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "시도 코드를 입력해주세요.");
        }
        
        if (!ctyCd.matches("\\d{2}")) {
            throw new ValidationException(Status.파라미터오류, "시도 코드는 2자리 숫자여야 합니다.");
        }
    }
    
    /**
     * 광역권 코드 검증
     */
    private void validateMegaCode(String megaCd) throws ValidationException {
        if (megaCd == null || megaCd.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "광역권 코드를 입력해주세요.");
        }
        
        if (megaCd.trim().length() < 1 || megaCd.trim().length() > 3) {
            throw new ValidationException(Status.파라미터오류, "광역권 코드는 1-3자리여야 합니다.");
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
            log.warn("[REGION-V1] 한국 영역 외부 좌표 - 위도: {}, 경도: {}", lat, lng);
        }
    }
    
    /**
     * 폴리곤 검증
     */
    private void validatePolygon(String polygon) throws ValidationException {
        if (polygon == null || polygon.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "폴리곤 데이터를 입력해주세요.");
        }
        
        if (polygon.trim().length() < 10) {
            throw new ValidationException(Status.파라미터오류, "유효하지 않은 폴리곤 데이터입니다.");
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
            throw new ValidationException(Status.파라미터오류, "반경은 최대 1000m까지 설정 가능합니다.");
        }
    }
    
    // ==================== 유틸리티 메서드 ====================
    
    /**
     * GeoJSON을 WKT 형식으로 변환
     * 
     * @param polygon GeoJSON 또는 WKT 폴리곤 문자열
     * @return WKT 형식 폴리곤
     */
    private String convertToWKT(String polygon) throws ValidationException {
        // 이미 WKT 형식인 경우
        if (polygon.trim().toUpperCase().startsWith("POLYGON")) {
            return polygon;
        }
        
        // GeoJSON 형식인 경우 (간단한 변환 - 실제로는 라이브러리 사용 권장)
        if (polygon.trim().startsWith("{") && polygon.contains("coordinates")) {
            log.warn("[REGION-V1] GeoJSON 형식 감지됨. WKT 변환 로직이 필요합니다.");
            throw new ValidationException(Status.파라미터오류, "현재 WKT 형식만 지원합니다. 예: POLYGON((lng lat, lng lat, ...))");
        }
        
        return polygon;
    }

    // ==================== 시도 목록 API ====================
    
    @Override
    public List<Map<String, Object>> getMegaList() throws ValidationException {
        log.info("[REGION-V1] 시도 목록 조회 서비스");
        
        try {
            // 데이터 조회
            List<Map<String, Object>> result = regionMapper.getMegaList();
            
            if (result == null || result.isEmpty()) {
                log.warn("[REGION-V1] 시도 목록 조회 결과 없음");
                throw new ValidationException(Status.데이터없음, "시도 목록을 찾을 수 없습니다.");
            }
            
            log.info("[REGION-V1] 시도 목록 조회 성공 - 조회건수: {}", result.size());
            return result;
            
        } catch (ValidationException e) {
            log.warn("[REGION-V1] 시도 목록 조회 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[REGION-V1] 시도 목록 조회 처리 오류", e);
            throw new ValidationException(Status.실패, "시도 목록 조회 중 오류가 발생했습니다.");
        }
    }
}