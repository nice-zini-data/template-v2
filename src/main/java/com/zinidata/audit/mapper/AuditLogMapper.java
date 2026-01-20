package com.zinidata.audit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.zinidata.audit.vo.AuditLogVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 DB 매핑 인터페이스
 * 
 * <p>감사 로그 데이터 처리를 위한 MyBatis 매퍼입니다.</p>
 * <p>기본적인 CRUD 뿐만 아니라 보안, 성능, 장애 모니터링을 위한 전문적인 메서드들을 제공합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface AuditLogMapper {
    
    // ==================== 기본 CRUD ====================
    
    /**
     * 감사 로그 삽입
     * 
     * @param auditLogVO 감사 로그 데이터
     * @return 영향받은 레코드 수
     */
    int insertAuditLog(AuditLogVO auditLogVO);
    
    // ==================== 기본 조회 ====================
    
    /**
     * 특정 회원의 감사 로그 조회
     * 
     * @param memNo 회원 번호
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 조회 개수 제한
     * @return 감사 로그 목록
     */
    List<AuditLogVO> selectAuditLogsByMemNo(
            @Param("memNo") Long memNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") Integer limit
    );
    
    /**
     * 특정 액션 타입의 감사 로그 조회
     * 
     * @param actionType 액션 타입
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 조회 개수 제한
     * @return 감사 로그 목록
     */
    List<AuditLogVO> selectAuditLogsByActionType(
            @Param("actionType") String actionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") Integer limit
    );
    
    /**
     * 특정 기간의 감사 로그 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 조회 개수 제한
     * @return 감사 로그 목록
     */
    List<AuditLogVO> selectAuditLogsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") Integer limit
    );
    
    // ==================== 보안 모니터링 ====================
    
    /**
     * 실패한 로그인 시도 조회 (보안 모니터링용)
     * 
     * <p>브루트 포스 공격 탐지를 위해 특정 IP에서 발생한 로그인 실패 횟수를 조회합니다.</p>
     * 
     * @param clientIp 클라이언트 IP
     * @param minutes 조회할 분 단위 (현재 시간 기준)
     * @return 실패한 로그인 시도 횟수
     */
    int countFailedLoginAttempts(
            @Param("clientIp") String clientIp,
            @Param("minutes") Integer minutes
    );
    
    /**
     * 특정 회원의 실패한 로그인 시도 조회
     * 
     * @param memNo 회원 번호
     * @param minutes 조회할 분 단위 (현재 시간 기준)
     * @return 실패한 로그인 시도 횟수
     */
    int countFailedLoginAttemptsByMemNo(
            @Param("memNo") Long memNo,
            @Param("minutes") Integer minutes
    );
    
    /**
     * 무단 액세스 시도 조회 (UNAUTHORIZED, FORBIDDEN)
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 조회 개수 제한
     * @return 무단 액세스 시도 목록
     */
    List<AuditLogVO> selectUnauthorizedAttempts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") Integer limit
    );
    
    // ==================== 성능 모니터링 ====================
    
    /**
     * 처리 시간이 오래 걸린 요청 조회 (성능 모니터링용)
     * 
     * <p>성능 병목 지점을 파악하기 위해 처리 시간이 임계값을 초과한 요청들을 조회합니다.</p>
     * 
     * @param thresholdMs 임계값 (밀리초)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 조회 개수 제한
     * @return 느린 요청 목록
     */
    List<AuditLogVO> selectSlowRequests(
            @Param("thresholdMs") Long thresholdMs,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") Integer limit
    );
    
    /**
     * 액션 타입별 평균 처리 시간 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 액션 타입별 평균 처리 시간 통계
     */
    List<AuditLogVO> selectAverageProcessingTimeByActionType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // ==================== 장애 모니터링 ====================
    
    /**
     * 에러 로그 조회 (장애 모니터링용)
     * 
     * <p>시스템 장애를 조기에 탐지하기 위해 에러 로그들을 조회합니다.</p>
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 조회 개수 제한
     * @return 에러 로그 목록
     */
    List<AuditLogVO> selectErrorLogs(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") Integer limit
    );
    
    /**
     * 특정 URI의 에러 발생 횟수 조회
     * 
     * @param requestUri 요청 URI
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 에러 발생 횟수
     */
    int countErrorsByRequestUri(
            @Param("requestUri") String requestUri,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // ==================== 통계 및 분석 ====================
    
    /**
     * 일별 액션 타입별 통계 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 일별 액션 타입별 통계
     */
    List<AuditLogVO> selectDailyStatsByActionType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 시간대별 접속 통계 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 시간대별 접속 통계
     */
    List<AuditLogVO> selectHourlyAccessStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // ==================== 데이터 관리 ====================
    
    /**
     * 오래된 로그 삭제 (데이터 정리용)
     * 
     * <p>스토리지 비용 최적화를 위해 보관 기간이 지난 로그들을 삭제합니다.</p>
     * 
     * @param retentionMonths 보관 기간 (개월)
     * @return 삭제된 행 수
     */
    int deleteOldLogs(@Param("retentionMonths") Integer retentionMonths);
    
    /**
     * 로그 테이블 통계 조회
     * 
     * @return 로그 테이블 통계 정보
     */
    AuditLogVO selectTableStatistics();
} 