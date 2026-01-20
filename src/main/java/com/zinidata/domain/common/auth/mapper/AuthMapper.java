package com.zinidata.domain.common.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.zinidata.domain.common.auth.vo.MemberVO;

import java.util.List;
import java.util.Map;

/**
 * 인증 Mapper
 * 
 * @author ZiniData 개발팀
 * @since 2.0
 */
@Mapper
public interface AuthMapper {

    /**
     * 로그인 검증
     */
    
    MemberVO getMember(@Param("loginId") String loginId, @Param("encryptedPwd") String encryptedPwd, @Param("appCode") String appCode);

    /**
     * 로그인 세션 정보 업데이트
     * token 미사용으로 session 정보만 업데이트
     */
    void updateMemberSession(@Param("memNo") Long memNo, @Param("loginSession") String loginSession, @Param("token") String token, @Param("loginTimestamp") Long loginTimestamp);

    /**
     * 휴대폰 번호 중복 가입 체크
     */
    int existsByMobileNo(@Param("mobileNo") String mobileNo, @Param("appCode") String appCode);
    
    /**
     * 로그인 ID 중복 가입 체크
     */
    int existsByLoginId(@Param("loginId") String loginId, @Param("appCode") String appCode);
    
    /**
     * 이메일 중복 가입 체크
     */
    int existsByEmailAddr(@Param("emailAddr") String emailAddr);
    
    /**
     * 카카오 ID 존재 여부 체크 (전체 - 활성+탈퇴)
     */
    int existsByKakaoId(@Param("kakaoId") String kakaoId);
    
    /**
     * 구글 ID 존재 여부 체크 (전체 - 활성+탈퇴)
     */
    int existsByGoogleId(@Param("googleId") String googleId);

    /**
     * 회원일련번호 가져오기
     */
    Long getMemberSeq();
    
    /**
     * 회원가입
     */
    int insertMember(MemberVO requestVo);

    /**
     * 회원 정보 수정
     */
    int updateMember(MemberVO requestVo);

    /**
     * tb_members_nvps 테이블에 회원 정보 insert
     */
    int insertMemberNvps(@Param("memNo") Long memNo, @Param("nvpsInfo") Map<String, Object> nvpsInfo);

    /**
     * tb_members_nvps 테이블에 회원 정보 update
     */
    int updateMemberNvps(@Param("memNo") Long memNo, @Param("nvpsInfo") Map<String, Object> nvpsInfo);

    /**
     * 비밀번호 변경
     */
    int updatePassword(@Param("memNo") Long memNo, @Param("password") String password);

    /**
     * 회원 탈퇴 (상태 변경)
     */
    int deleteMember(@Param("memNo") Long memNo);

    /**
     * 로그인 ID로 회원 조회
     */
    MemberVO findByLoginId(MemberVO requestVo);

    /**
     * 이메일로 회원 조회
     */
    MemberVO findByEmailAddr(@Param("emailAddr") String emailAddr);

    /**
     * 휴대폰 번호로 회원 조회
     */
    MemberVO findByMobileNo(@Param("mobileNo") String mobileNo);

    /**
     * 이름과 휴대폰 번호로 회원 조회 (아이디 찾기용)
     */
    MemberVO findByMemNmAndMobileNo(MemberVO requestVo);

    /**
     * 로그인 ID와 휴대폰 번호로 회원 조회 (비밀번호 찾기용)
     */
    MemberVO findByLoginIdAndMobileNo(MemberVO requestVo);

    /**
     * 회원 번호로 회원 조회
     */
    MemberVO findByMemNo(@Param("memNo") Long memNo, @Param("appCode") String appCode);

    /**
     * 카카오 ID로 회원 조회
     */
    MemberVO findByKakaoId(@Param("kakaoId") String kakaoId);

    /**
     * 구글 ID로 회원 조회
     */
    MemberVO findByGoogleId(@Param("googleId") String googleId);

    /**
     * 회원 목록 조회 (관리자용)
     */
    List<MemberVO> findMembers(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 회원 수 조회 (관리자용)
     */
    int countMembers();

    /**
     * 회원 검색 (관리자용)
     */
    List<MemberVO> searchMembers(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 회원 검색 수 조회 (관리자용)
     */
    int countSearchMembers(@Param("keyword") String keyword);

    /**
     * 활성 회원 수 조회
     */
    int countActiveMembers();

    /**
     * 최근 가입 회원 조회
     */
    List<MemberVO> findRecentMembers(@Param("days") int days);

    /**
     * 회원 통계 조회
     */
    Map<String, Object> getMemberStatistics();

    /**
     * 회원 상태별 통계 조회
     */
    List<Map<String, Object>> getMemberStatusStatistics();

    /**
     * 회원 타입별 통계 조회
     */
    List<Map<String, Object>> getMemberTypeStatistics();

    /**
     * 월별 가입 회원 통계 조회
     */
    List<Map<String, Object>> getMonthlySignupStatistics(@Param("year") int year);

    /**
     * 회원 활동 로그 조회
     */
    List<Map<String, Object>> getMemberActivityLogs(@Param("memNo") Long memNo, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 회원 활동 로그 수 조회
     */
    int countMemberActivityLogs(@Param("memNo") Long memNo);

    /**
     * 회원 세션 정보 조회
     */
    Map<String, Object> getMemberSessionInfo(@Param("memNo") Long memNo);

    /**
     * 회원 세션 정보 업데이트
     */
    int updateMemberSessionInfo(@Param("memNo") Long memNo, @Param("sessionInfo") Map<String, Object> sessionInfo);

    /**
     * 만료된 세션 정리
     */
    int cleanupExpiredSessions(@Param("expiredBefore") Long expiredBefore);

    /**
     * 회원 로그인 이력 조회
     */
    List<Map<String, Object>> getMemberLoginHistory(@Param("memNo") Long memNo, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 회원 로그인 이력 수 조회
     */
    int countMemberLoginHistory(@Param("memNo") Long memNo);

    /**
     * 회원 로그인 이력 저장
     */
    int insertMemberLoginHistory(@Param("memNo") Long memNo, @Param("loginInfo") Map<String, Object> loginInfo);

    /**
     * 로그인 인증 이력 저장 (tb_log_auth)
     */
    int insertLogAuth(@Param("memNo") Long memNo, @Param("sessionId") String sessionId, @Param("ipAddr") String ipAddr);

    /**
     * 회원 접속 통계 조회
     */
    Map<String, Object> getMemberAccessStatistics(@Param("memNo") Long memNo);

    /**
     * 회원 접속 통계 업데이트
     */
    int updateMemberAccessStatistics(@Param("memNo") Long memNo, @Param("accessInfo") Map<String, Object> accessInfo);

    /**
     * 회원 보안 설정 조회
     */
    Map<String, Object> getMemberSecuritySettings(@Param("memNo") Long memNo);

    /**
     * 회원 보안 설정 업데이트
     */
    int updateMemberSecuritySettings(@Param("memNo") Long memNo, @Param("securitySettings") Map<String, Object> securitySettings);

    /**
     * 회원 알림 설정 조회
     */
    Map<String, Object> getMemberNotificationSettings(@Param("memNo") Long memNo);

    /**
     * 회원 알림 설정 업데이트
     */
    int updateMemberNotificationSettings(@Param("memNo") Long memNo, @Param("notificationSettings") Map<String, Object> notificationSettings);

    /**
     * 회원 프로필 이미지 업데이트
     */
    int updateMemberProfileImage(@Param("memNo") Long memNo, @Param("profileImageUrl") String profileImageUrl);

    /**
     * 회원 프로필 정보 업데이트
     */
    int updateMemberProfile(@Param("memNo") Long memNo, @Param("profileInfo") Map<String, Object> profileInfo);

    /**
     * 회원 계정 잠금
     */
    int lockMemberAccount(@Param("memNo") Long memNo, @Param("lockReason") String lockReason);

    /**
     * 회원 계정 잠금 해제
     */
    int unlockMemberAccount(@Param("memNo") Long memNo);

    /**
     * 회원 계정 잠금 이력 조회
     */
    List<Map<String, Object>> getMemberLockHistory(@Param("memNo") Long memNo);

    /**
     * 회원 계정 잠금 이력 저장
     */
    int insertMemberLockHistory(@Param("memNo") Long memNo, @Param("lockInfo") Map<String, Object> lockInfo);

    /**
     * 회원 비밀번호 변경 이력 조회
     */
    List<Map<String, Object>> getMemberPasswordChangeHistory(@Param("memNo") Long memNo);

    /**
     * 회원 비밀번호 변경 이력 저장
     */
    int insertMemberPasswordChangeHistory(@Param("memNo") Long memNo, @Param("changeInfo") Map<String, Object> changeInfo);

    /**
     * 회원 이메일 변경 이력 조회
     */
    List<Map<String, Object>> getMemberEmailChangeHistory(@Param("memNo") Long memNo);

    /**
     * 회원 이메일 변경 이력 저장
     */
    int insertMemberEmailChangeHistory(@Param("memNo") Long memNo, @Param("changeInfo") Map<String, Object> changeInfo);

    /**
     * 회원 휴대폰 번호 변경 이력 조회
     */
    List<Map<String, Object>> getMemberMobileChangeHistory(@Param("memNo") Long memNo);

    /**
     * 회원 휴대폰 번호 변경 이력 저장
     */
    int insertMemberMobileChangeHistory(@Param("memNo") Long memNo, @Param("changeInfo") Map<String, Object> changeInfo);

    /**
     * 회원 탈퇴 이력 조회
     */
    List<Map<String, Object>> getMemberWithdrawalHistory(@Param("memNo") Long memNo);

    /**
     * 회원 탈퇴 이력 저장
     */
    int insertMemberWithdrawalHistory(@Param("memNo") Long memNo, @Param("withdrawalInfo") Map<String, Object> withdrawalInfo);

    /**
     * 회원 복구
     */
    int restoreMember(@Param("memNo") Long memNo);

    /**
     * 회원 복구 이력 조회
     */
    List<Map<String, Object>> getMemberRestoreHistory(@Param("memNo") Long memNo);

    /**
     * 회원 복구 이력 저장
     */
    int insertMemberRestoreHistory(@Param("memNo") Long memNo, @Param("restoreInfo") Map<String, Object> restoreInfo);

    /**
     * 회원 데이터 내보내기
     */
    Map<String, Object> exportMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 삭제 (GDPR 준수)
     */
    int deleteMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 익명화
     */
    int anonymizeMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 백업
     */
    int backupMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 복원
     */
    int restoreMemberData(@Param("memNo") Long memNo, @Param("backupData") Map<String, Object> backupData);

    /**
     * 회원 데이터 검증
     */
    Map<String, Object> validateMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 무결성 검사
     */
    List<Map<String, Object>> checkMemberDataIntegrity();

    /**
     * 회원 데이터 정리
     */
    int cleanupMemberData(@Param("days") int days);

    /**
     * 회원 데이터 마이그레이션
     */
    int migrateMemberData(@Param("fromMemNo") Long fromMemNo, @Param("toMemNo") Long toMemNo);

    /**
     * 회원 데이터 동기화
     */
    int syncMemberData(@Param("memNo") Long memNo, @Param("externalData") Map<String, Object> externalData);

    /**
     * 회원 데이터 분석
     */
    Map<String, Object> analyzeMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 리포트 생성
     */
    Map<String, Object> generateMemberDataReport(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 압축
     */
    int compressMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 압축 해제
     */
    int decompressMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 암호화
     */
    int encryptMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 복호화
     */
    int decryptMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 검색
     */
    List<Map<String, Object>> searchMemberData(@Param("searchCriteria") Map<String, Object> searchCriteria);

    /**
     * 회원 데이터 필터링
     */
    List<Map<String, Object>> filterMemberData(@Param("filterCriteria") Map<String, Object> filterCriteria);

    /**
     * 회원 데이터 정렬
     */
    List<Map<String, Object>> sortMemberData(@Param("sortCriteria") Map<String, Object> sortCriteria);

    /**
     * 회원 데이터 그룹화
     */
    List<Map<String, Object>> groupMemberData(@Param("groupCriteria") Map<String, Object> groupCriteria);

    /**
     * 회원 데이터 집계
     */
    Map<String, Object> aggregateMemberData(@Param("aggregationCriteria") Map<String, Object> aggregationCriteria);

    /**
     * 회원 데이터 변환
     */
    Map<String, Object> transformMemberData(@Param("memNo") Long memNo, @Param("transformationRules") Map<String, Object> transformationRules);

    /**
     * 회원 데이터 검증 규칙 적용
     */
    Map<String, Object> applyMemberDataValidationRules(@Param("memNo") Long memNo, @Param("validationRules") Map<String, Object> validationRules);

    /**
     * 회원 데이터 품질 평가
     */
    Map<String, Object> evaluateMemberDataQuality(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 품질 개선
     */
    int improveMemberDataQuality(@Param("memNo") Long memNo, @Param("improvementRules") Map<String, Object> improvementRules);

    /**
     * 회원 데이터 모니터링
     */
    Map<String, Object> monitorMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 알림
     */
    int notifyMemberDataChanges(@Param("memNo") Long memNo, @Param("notificationInfo") Map<String, Object> notificationInfo);

    /**
     * 회원 데이터 로깅
     */
    int logMemberDataChanges(@Param("memNo") Long memNo, @Param("logInfo") Map<String, Object> logInfo);

    /**
     * 회원 데이터 감사
     */
    List<Map<String, Object>> auditMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 보안 검사
     */
    Map<String, Object> securityCheckMemberData(@Param("memNo") Long memNo);

    /**
     * 회원 데이터 권한 검사
     */
    Map<String, Object> permissionCheckMemberData(@Param("memNo") Long memNo, @Param("userId") Long userId);

    /**
     * 회원 데이터 접근 제어
     */
    int controlMemberDataAccess(@Param("memNo") Long memNo, @Param("accessControlRules") Map<String, Object> accessControlRules);

    /**
     * 회원 데이터 백업 스케줄링
     */
    int scheduleMemberDataBackup(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 복원 스케줄링
     */
    int scheduleMemberDataRestore(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 마이그레이션 스케줄링
     */
    int scheduleMemberDataMigration(@Param("fromMemNo") Long fromMemNo, @Param("toMemNo") Long toMemNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 동기화 스케줄링
     */
    int scheduleMemberDataSync(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 분석 스케줄링
     */
    int scheduleMemberDataAnalysis(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 리포트 생성 스케줄링
     */
    int scheduleMemberDataReport(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 압축 스케줄링
     */
    int scheduleMemberDataCompression(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 암호화 스케줄링
     */
    int scheduleMemberDataEncryption(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 검색 스케줄링
     */
    int scheduleMemberDataSearch(@Param("searchCriteria") Map<String, Object> searchCriteria, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 필터링 스케줄링
     */
    int scheduleMemberDataFiltering(@Param("filterCriteria") Map<String, Object> filterCriteria, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 정렬 스케줄링
     */
    int scheduleMemberDataSorting(@Param("sortCriteria") Map<String, Object> sortCriteria, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 그룹화 스케줄링
     */
    int scheduleMemberDataGrouping(@Param("groupCriteria") Map<String, Object> groupCriteria, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 집계 스케줄링
     */
    int scheduleMemberDataAggregation(@Param("aggregationCriteria") Map<String, Object> aggregationCriteria, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 변환 스케줄링
     */
    int scheduleMemberDataTransformation(@Param("memNo") Long memNo, @Param("transformationRules") Map<String, Object> transformationRules, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 검증 스케줄링
     */
    int scheduleMemberDataValidation(@Param("memNo") Long memNo, @Param("validationRules") Map<String, Object> validationRules, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 품질 평가 스케줄링
     */
    int scheduleMemberDataQualityEvaluation(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 품질 개선 스케줄링
     */
    int scheduleMemberDataQualityImprovement(@Param("memNo") Long memNo, @Param("improvementRules") Map<String, Object> improvementRules, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 모니터링 스케줄링
     */
    int scheduleMemberDataMonitoring(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 알림 스케줄링
     */
    int scheduleMemberDataNotification(@Param("memNo") Long memNo, @Param("notificationInfo") Map<String, Object> notificationInfo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 로깅 스케줄링
     */
    int scheduleMemberDataLogging(@Param("memNo") Long memNo, @Param("logInfo") Map<String, Object> logInfo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 감사 스케줄링
     */
    int scheduleMemberDataAudit(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 보안 검사 스케줄링
     */
    int scheduleMemberDataSecurityCheck(@Param("memNo") Long memNo, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 권한 검사 스케줄링
     */
    int scheduleMemberDataPermissionCheck(@Param("memNo") Long memNo, @Param("userId") Long userId, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

    /**
     * 회원 데이터 접근 제어 스케줄링
     */
    int scheduleMemberDataAccessControl(@Param("memNo") Long memNo, @Param("accessControlRules") Map<String, Object> accessControlRules, @Param("scheduleInfo") Map<String, Object> scheduleInfo);

}
