package com.zinidata.domain.common.upjong.service;

import java.util.List;
import java.util.Map;

/**
 * 분석업종 서비스 인터페이스
 * 
 * <p>분석업종(upjong1/upjong2/upjong3) 관련 비즈니스 로직을 정의합니다.</p>
 * 
 * <h3>구현체 선택 방식</h3>
 * <ul>
 *   <li><strong>UpjongServiceImpl</strong>: 기본 데이터베이스 구현체 (현재 사용 중)</li>
 *   <li><strong>UpjongServiceRedisImpl</strong>: Redis 캐시 구현체 (성능 최적화용, 미사용)</li>
 * </ul>
 * 
 * <p><strong>설정 방법:</strong></p>
 * <pre>
 * # application.yml
 * upjong:
 *   datasource: database  # 기본값 (설정 없어도 자동 선택)
 *   datasource: redis     # Redis 캐시 사용 시
 * </pre>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
public interface UpjongService {
    
    /**
     * 분석업종 목록 조회
     * 
     * @param level 업종 레벨 (upjong1/upjong2/upjong3)
     * @param upjongCode 상위 업종 코드 (upjong2일때: 대분류코드, upjong3일때: 중분류코드, null: 전체)
     * @return 업종 목록
     */
    List<Map<String, Object>> getUpjongList(String level, String upjongCode);
    
    /**
     * 분석업종 목록 조회 (하위 호환용)
     * 
     * @param level 업종 레벨 (upjong1/upjong2/upjong3)
     * @return 업종 목록
     */
    default List<Map<String, Object>> getUpjongList(String level) {
        return getUpjongList(level, null);
    }
    
    /**
     * 업종 계층구조 조회
     * 
     * @param upjongCode 업종 코드 (null: 전체, 1자리: 대분류, 3자리: 중분류, 6자리: 소분류)
     * @return 업종 계층구조
     */
    Map<String, Object> getUpjongHierarchy(String upjongCode);
    
    /**
     * 전체 업종 계층구조 조회 (하위 호환용)
     * 
     * @return 전체 업종 계층구조
     */
    default Map<String, Object> getUpjongHierarchy() {
        return getUpjongHierarchy(null);
    }
    
    /**
     * 특정 행정동의 분석 가능한 업종 목록 조회
     * 
     * @param admiCd 행정동 코드
     * @return 분석 가능한 업종 목록 (가맹점 수 >= 3인 업종들)
     */
    List<Map<String, Object>> getAnalyzableUpjongList(String admiCd);
    
    /**
     * 특정 행정동의 업종별 가맹점 수 조회
     * 
     * @param admiCd 행정동 코드
     * @return 업종별 가맹점 수 정보
     */
    Map<String, Object> getUpjongStoreCount(String admiCd);
    
    /**
     * 특정 행정동의 특정 업종 분석 가능 여부 체크
     * 
     * @param admiCd 행정동 코드
     * @param upjong3Cd 소분류 업종 코드
     * @return 분석 가능 여부 (가맹점 수 >= 3)
     */
    boolean isAnalyzableUpjong(String admiCd, String upjong3Cd);
    
    /**
     * 업종명으로 업종 검색
     * 
     * @param upjong3Nm 소분류 업종명 (LIKE 검색)
     * @return 검색된 업종들의 계층구조
     */
    Map<String, Object> searchUpjongByName(String upjong3Nm);
    
    /**
     * 업종명으로 업종 검색 (확장 점포수 기반)
     * 
     * @param keyword 검색할 업종명
     * @param admiCd 행정동 코드 (8자리)
     * @return 검색된 업종들의 계층구조 (확장 점포수 및 분석가능 여부 포함)
     */
    Map<String, Object> searchUpjongByNameWithStore(String keyword, String admiCd);
} 