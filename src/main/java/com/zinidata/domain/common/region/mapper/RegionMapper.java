package com.zinidata.domain.common.region.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 지역 정보 데이터 액세스 매퍼
 * 
 * <p>블록, 행정동, 시군구, 광역시도 관련 데이터베이스 쿼리를 처리합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface RegionMapper {

    // ==================== 블록 쿼리 ====================
    
    /**
     * 블록 코드로 블록 정보 조회
     * 
     * @param blkCd 블록 코드 (6자리)
     * @return 블록 정보
     */
    Map<String, Object> selectBlockByCode(@Param("blkCd") String blkCd);
    
    /**
     * 좌표로 블록 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 블록 정보
     */
    Map<String, Object> selectBlockByPoint(@Param("lat") double lat, @Param("lng") double lng);
    
    /**
     * 폴리곤 영역 내 블록 조회
     * 
     * @param polygon WKT 폴리곤 문자열
     * @return 블록 목록
     */
    List<Map<String, Object>> selectBlocksByPolygon(@Param("polygon") String polygon);
    
    /**
     * 반경 내 블록 조회
     * 
     * @param lat 중심점 위도
     * @param lng 중심점 경도
     * @param radius 반경 (미터)
     * @return 블록 목록 (거리 포함)
     */
    List<Map<String, Object>> selectBlocksByRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") int radius);

    // ==================== 행정동 쿼리 ====================
    
    /**
     * 행정동 코드로 행정동 정보 조회
     * 
     * @param admiCd 행정동 코드 (8자리)
     * @return 행정동 정보
     */
    Map<String, Object> selectAdmiByCode(@Param("admiCd") String admiCd);
    
    /**
     * 좌표로 행정동 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 행정동 정보
     */
    Map<String, Object> selectAdmiByPoint(@Param("lat") double lat, @Param("lng") double lng);
    
    /**
     * 폴리곤 영역 내 행정동 조회
     * 
     * @param polygon WKT 폴리곤 문자열
     * @return 행정동 목록
     */
    List<Map<String, Object>> selectAdmisByPolygon(@Param("polygon") String polygon);
    
    /**
     * 인접 행정동 조회
     * 
     * @param admiCd 기준 행정동 코드 (8자리)
     * @return 인접 행정동 목록 (기준 행정동 포함)
     */
    List<Map<String, Object>> selectAdjacentAdmis(@Param("admiCd") String admiCd);

    // ==================== 시군구 쿼리 ====================
    
    /**
     * 시군구 코드로 시군구 정보 조회
     * 
     * @param ctyCd 시군구 코드 (4자리)
     * @return 시군구 정보
     */
    Map<String, Object> selectCtyByCode(@Param("ctyCd") String ctyCd);
    
    /**
     * 좌표로 시군구 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 시군구 정보
     */
    Map<String, Object> selectCtyByPoint(@Param("lat") double lat, @Param("lng") double lng);
    
    /**
     * 폴리곤 영역 내 시군구 조회
     * 
     * @param polygon WKT 폴리곤 문자열
     * @return 시군구 목록
     */
    List<Map<String, Object>> selectCtysByPolygon(@Param("polygon") String polygon);

    // ==================== 광역시도 쿼리 ====================
    
    /**
     * 광역시도 코드로 광역시도 정보 조회
     * 
     * @param megaCd 광역시도 코드 (2자리)
     * @return 광역시도 정보
     */
    Map<String, Object> selectMegaByCode(@Param("megaCd") String megaCd);
    
    /**
     * 좌표로 광역시도 조회
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 광역시도 정보
     */
    Map<String, Object> selectMegaByPoint(@Param("lat") double lat, @Param("lng") double lng);
    
    /**
     * 폴리곤 영역 내 광역시도 조회
     * 
     * @param polygon WKT 폴리곤 문자열
     * @return 광역시도 목록
     */
    List<Map<String, Object>> selectMegasByPolygon(@Param("polygon") String polygon);

    /**
     * 확장 분석 행정동 geometry 정보 조회
     * 
     * @param admiCd 행정동코드 (8자리)
     * @param upjong3Cd 소분류업종코드 (6자리)
     * @param yyyymm 분석기준년월
     * @return 확장 분석 행정동 geometry 정보 목록 (admiCd, admiNm, centerX, centerY, feature 포함)
     */
    List<Map<String, Object>> getExpandedAdmiRegions(@Param("admiCd") String admiCd, @Param("upjong3Cd") String upjong3Cd, @Param("yyyymm") String yyyymm);

    /**
     * 시도 목록 조회
     * 
     * @return 시도 목록
     */
    List<Map<String, Object>> getMegaList();
} 