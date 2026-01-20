package com.zinidata.domain.common.region.service;

import java.util.List;
import java.util.Map;

import com.zinidata.common.exception.ValidationException;

/**
 * 지역 정보 서비스 인터페이스
 * 
 * <p>블록, 행정동, 시군구, 광역시도 관련 비즈니스 로직을 정의합니다.</p>
 * <p>PostGIS 기반 공간 데이터 처리 및 GIS 분석 기능을 제공합니다.</p>
 * 
 * <h3>구현체 선택 방식</h3>
 * <ul>
 *   <li><strong>RegionServiceImpl</strong>: 기본 데이터베이스 구현체 (현재 사용 중)</li>
 *   <li><strong>RegionServiceRedisImpl</strong>: Redis 캐시 구현체 (성능 최적화용, 미사용)</li>
 * </ul>
 * 
 * <p><strong>설정 방법:</strong></p>
 * <pre>
 * # application.yml
 * region:
 *   datasource: database  # 기본값 (설정 없어도 자동 선택)
 *   datasource: redis     # Redis 캐시 사용 시
 * </pre>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>🗺️ <strong>공간 검색:</strong> 좌표 기반 지역 조회 (Point in Polygon)</li>
 *   <li>📍 <strong>코드 검색:</strong> 행정구역 코드로 정보 조회</li>
 *   <li>🔍 <strong>영역 검색:</strong> 폴리곤/반경 내 지역 목록 조회</li>
 *   <li>📊 <strong>확장 분석:</strong> 업종별 행정동 확장 분석 지원</li>
 * </ul>
 * 
 * <h3>지원 행정구역</h3>
 * <ul>
 *   <li><strong>블록:</strong> 6자리 코드, 최소 공간 단위</li>
 *   <li><strong>행정동:</strong> 8자리 코드, 메인 분석 단위</li>
 *   <li><strong>시군구:</strong> 4자리 코드, 중간 행정 단위</li>
 *   <li><strong>광역시도:</strong> 2자리 코드, 최상위 행정 단위</li>
 * </ul>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
public interface RegionService {

    // ==================== 블록 API ====================
    
    /**
     * 블록 코드로 블록 정보 조회
     * 
     * @param blkCd 블록 코드 (6자리)
     * @return 블록 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getBlockByCode(String blkCd) throws ValidationException;
    
    /**
     * 좌표로 블록 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 블록 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getBlockByPoint(double lat, double lng) throws ValidationException;
    
    /**
     * 폴리곤 영역 내 블록 조회
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 블록 목록
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getBlocksByPolygon(String polygon) throws ValidationException;
    
    /**
     * 반경 내 블록 조회
     * 
     * @param lat 중심점 위도
     * @param lng 중심점 경도
     * @param radius 반경 (미터)
     * @return 블록 목록
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getBlocksByRadius(double lat, double lng, int radius) throws ValidationException;

    // ==================== 행정동 API ====================
    
    /**
     * 행정동 코드로 행정동 정보 조회
     * 
     * @param admiCd 행정동 코드 (8자리)
     * @return 행정동 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getAdmiByCode(String admiCd) throws ValidationException;
    
    /**
     * 좌표로 행정동 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 행정동 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getAdmiByPoint(double lat, double lng) throws ValidationException;
    
    /**
     * 폴리곤 영역 내 행정동 조회
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 행정동 목록
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getAdmisByPolygon(String polygon) throws ValidationException;
    
    /**
     * 인접 행정동 조회
     * 
     * @param admiCd 기준 행정동 코드 (8자리)
     * @return 인접 행정동 목록 (기준 행정동 포함)
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getAdjacentAdmis(String admiCd) throws ValidationException;

    // ==================== 시군구 API ====================
    
    /**
     * 시도 코드로 시도 정보 조회
     * 
     * @param ctyCd 시도 코드 (2자리)
     * @return 시도 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getCtyByCode(String ctyCd) throws ValidationException;
    
    /**
     * 좌표로 시도 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 시도 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getCtyByPoint(double lat, double lng) throws ValidationException;
    
    /**
     * 폴리곤 영역 내 시도 조회
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 시도 목록
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getCtysByPolygon(String polygon) throws ValidationException;

    // ==================== 광역시도 API ====================
    
    /**
     * 광역권 코드로 광역권 정보 조회
     * 
     * @param megaCd 광역권 코드
     * @return 광역권 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getMegaByCode(String megaCd) throws ValidationException;
    
    /**
     * 좌표로 광역권 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 광역권 정보
     * @throws ValidationException 검증 실패 시
     */
    Map<String, Object> getMegaByPoint(double lat, double lng) throws ValidationException;
    
    /**
     * 폴리곤 영역 내 광역권 조회
     * 
     * @param polygon GeoJSON 폴리곤 문자열
     * @return 광역권 목록
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getMegasByPolygon(String polygon) throws ValidationException;

    /**
     * 시도 목록 조회
     * 
     * @return 시도 목록
     * @throws ValidationException 검증 실패 시
     */
    List<Map<String, Object>> getMegaList() throws ValidationException;
} 