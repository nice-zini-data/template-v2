package com.zinidata.domain.common.upjong.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 분석업종 매퍼 인터페이스
 * 
 * <p>분석업종(upjong1/upjong2/upjong3) 관련 데이터 액세스를 담당합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface UpjongMapper {
    
    /**
     * 대분류 업종 목록 조회
     * 
     * @return 대분류 업종 목록
     */
    List<Map<String, Object>> selectUpjong1List();
    
    /**
     * 중분류 업종 목록 조회
     * 
     * @param upjong1Cd 대분류 업종 코드 (선택적)
     * @return 중분류 업종 목록
     */
    List<Map<String, Object>> selectUpjong2List(@Param("upjong1Cd") String upjong1Cd);
    
    /**
     * 소분류 업종 목록 조회
     * 
     * @param upjong2Cd 중분류 업종 코드 (선택적)
     * @return 소분류 업종 목록
     */
    List<Map<String, Object>> selectUpjong3List(@Param("upjong2Cd") String upjong2Cd);
    
    /**
     * 전체 업종 계층구조 조회
     * 
     * @return 3단계 업종 계층구조 원본 데이터
     */
    List<Map<String, Object>> selectUpjongHierarchy();
    
    /**
     * 업종 계층구조 조회 (필터링 가능)
     * 
     * @param upjongCode 업종 코드 (null: 전체, 1자리: 대분류, 3자리: 중분류, 6자리: 소분류)
     * @return 업종 계층구조 원본 데이터
     */
    List<Map<String, Object>> selectUpjongHierarchyByCode(@Param("upjongCode") String upjongCode);
    
    /**
     * 특정 행정동의 업종별 가맹점 수 조회
     * 
     * @param admiCd 행정동 코드
     * @return 업종별 가맹점 수 정보
     */
    List<Map<String, Object>> selectUpjongStoreCountByAdmi(@Param("admiCd") String admiCd);
    
    /**
     * 업종명으로 업종 검색
     * 
     * @param upjong3Nm 소분류 업종명 (LIKE 검색)
     * @return 검색된 업종들의 중분류 코드 목록
     */
    List<Map<String, Object>> selectUpjongByName(@Param("upjong3Nm") String upjong3Nm);
    
    /**
     * 특정 중분류 코드들의 전체 계층구조 조회
     * 
     * @param upjong2Cds 중분류 코드 목록
     * @return 해당 중분류들의 전체 계층구조
     */
    List<Map<String, Object>> selectUpjongHierarchyByCodes(@Param("upjong2Cds") List<String> upjong2Cds);

    /**
     * 특정 중분류 코드들의 전체 계층구조 조회 + 확장 점포수/분석가능 여부 포함
     *
     * @param upjong2Cds 중분류 코드 목록
     * @param admiCd 행정동 코드(8자리)
     * @return 계층구조 + storeCnt/analyzable
     */
    List<Map<String, Object>> selectUpjongHierarchyByCodesWithExpandedStore(
            @Param("upjong2Cds") List<String> upjong2Cds,
            @Param("admiCd") String admiCd
    );
} 