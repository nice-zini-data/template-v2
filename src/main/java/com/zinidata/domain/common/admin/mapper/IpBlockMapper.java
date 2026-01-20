package com.zinidata.domain.common.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.zinidata.domain.common.admin.vo.IpBlockVO;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * IP 차단 관리 Mapper
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface IpBlockMapper {

    /**
     * IP 주소와 프로젝트 코드로 활성 차단 조회
     * 
     * @param ipAddress IP 주소
     * @param appCode 프로젝트 코드
     * @return 활성 차단 정보
     */
    IpBlockVO findActiveBlock(@Param("ipAddress") String ipAddress, 
                             @Param("appCode") String appCode);

    /**
     * IP 주소로 활성 차단 여부 확인
     * 
     * @param ipAddress IP 주소
     * @param appCode 프로젝트 코드
     * @return 차단 여부 (0: 차단 없음, 1: 차단 있음)
     */
    int existsActiveBlock(@Param("ipAddress") String ipAddress, 
                         @Param("appCode") String appCode);

    /**
     * 프로젝트별 모든 활성 차단 목록 조회
     * 
     * @param appCode 프로젝트 코드
     * @return 활성 차단 목록
     */
    List<IpBlockVO> findAllActiveBlocks(@Param("appCode") String appCode);

    /**
     * 프로젝트별 모든 차단 목록 조회 (상태 무관)
     * 
     * @param appCode 프로젝트 코드
     * @return 모든 차단 목록
     */
    List<IpBlockVO> findAllByProjectCode(@Param("appCode") String appCode);

    /**
     * IP 주소로 차단 이력 조회
     * 
     * @param ipAddress IP 주소
     * @param appCode 프로젝트 코드
     * @return 차단 이력 목록
     */
    List<IpBlockVO> findAllByIpAddressAndProjectCode(@Param("ipAddress") String ipAddress, 
                                                    @Param("appCode") String appCode);

    /**
     * 특정 상태의 차단 목록 조회
     * 
     * @param status 차단 상태
     * @param appCode 프로젝트 코드
     * @return 해당 상태의 차단 목록
     */
    List<IpBlockVO> findAllByStatusAndProjectCode(@Param("status") String status, 
                                                 @Param("appCode") String appCode);

    /**
     * 만료된 임시 차단 목록 조회
     * 
     * @param currentTime 현재 시간
     * @return 만료된 차단 목록
     */
    List<IpBlockVO> findExpiredBlocks(@Param("currentTime") Timestamp currentTime);

    /**
     * IP 차단 등록
     * 
     * @param ipBlockVO IP 차단 정보
     * @return 등록된 레코드 수
     */
    int insertIpBlock(IpBlockVO ipBlockVO);

    /**
     * IP 차단 해제
     * 
     * @param ipAddress IP 주소
     * @param appCode 프로젝트 코드
     * @param unblockedBy 해제자
     * @param currentTime 현재 시간
     * @return 해제된 레코드 수
     */
    int unblockIp(@Param("ipAddress") String ipAddress,
                  @Param("appCode") String appCode,
                  @Param("unblockedBy") String unblockedBy,
                  @Param("currentTime") Timestamp currentTime);

    /**
     * 특정 IP 목록의 활성 차단 일괄 해제
     * 
     * @param ipAddresses IP 주소 목록
     * @param appCode 프로젝트 코드
     * @param unblockedBy 해제자
     * @param currentTime 현재 시간
     * @return 해제된 레코드 수
     */
    int unblockIpsBulk(@Param("ipAddresses") Set<String> ipAddresses,
                       @Param("appCode") String appCode,
                       @Param("unblockedBy") String unblockedBy,
                       @Param("currentTime") Timestamp currentTime);

    /**
     * 만료된 차단 상태 일괄 업데이트
     * 
     * @param currentTime 현재 시간
     * @return 업데이트된 레코드 수
     */
    int updateExpiredBlocks(@Param("currentTime") Timestamp currentTime);

    /**
     * 특정 기간 이전의 해제된 차단 기록 삭제 (정리용)
     * 
     * @param beforeDate 삭제 기준 날짜
     * @return 삭제된 레코드 수
     */
    int deleteOldUnblockedRecords(@Param("beforeDate") Timestamp beforeDate);

    /**
     * 프로젝트별 차단 통계 조회
     * 
     * @param appCode 프로젝트 코드
     * @return 통계 정보 Map
     */
    java.util.Map<String, Object> getBlockStatistics(@Param("appCode") String appCode);

    /**
     * 특정 등록자의 차단 목록 조회
     * 
     * @param blockedBy 등록자
     * @param appCode 프로젝트 코드
     * @return 차단 목록
     */
    List<IpBlockVO> findAllByBlockedByAndProjectCode(@Param("blockedBy") String blockedBy, 
                                                    @Param("appCode") String appCode);

    /**
     * 최근 N일간의 차단 목록 조회
     * 
     * @param fromDate 조회 시작 날짜
     * @param appCode 프로젝트 코드
     * @return 최근 차단 목록
     */
    List<IpBlockVO> findRecentBlocks(@Param("fromDate") Timestamp fromDate, 
                                    @Param("appCode") String appCode);

    /**
     * IP 패턴으로 차단 목록 검색
     * 
     * @param ipPattern IP 패턴 (LIKE 검색용)
     * @param appCode 프로젝트 코드
     * @return 검색된 차단 목록
     */
    List<IpBlockVO> findByIpPattern(@Param("ipPattern") String ipPattern, 
                                   @Param("appCode") String appCode);
} 