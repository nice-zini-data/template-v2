package com.zinidata.domain.common.auth.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

/**
 * 회원 VO (tb_member 테이블 매핑)
 * 
 * @author ZiniData 개발팀
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO {
    
    /** 회원번호 */
    private Long memNo;
    
    /** 프로젝트 코드 */
    private String appCode;
    
    /** 로그인 ID */
    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Size(min = 4, max = 20, message = "로그인 ID는 4-20자 사이여야 합니다.")
    private String loginId;
    
    /** 비밀번호 */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다.")
    private String password;
    
    private String newPassword;

    /** 회원상태 (ACTIVE: 활성, INACTIVE: 비활성, WITHDRAWN: 탈퇴) */
    @NotBlank(message = "회원상태는 필수입니다.")
    private String memStat;
    
    /** 회원명 */
    @NotBlank(message = "회원명은 필수입니다.")
    @Size(max = 50, message = "회원명은 50자 이하여야 합니다.")
    private String memNm;
    
    /** 가상주민번호 (미사용) */
    @Size(max = 50, message = "가상주민번호는 50자 이하여야 합니다.")
    private String vno;
    
    /** 휴대폰번호 */
    @Size(max = 20, message = "휴대폰번호는 20자 이하여야 합니다.")
    private String mobileNo;
    
    /** 이메일주소 */
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일주소는 100자 이하여야 합니다.")
    private String emailAddr;
    
    /** 회원타입 (USER: 일반회원, ADMIN: 관리자, PREMIUM: 프리미엄) */
    @NotBlank(message = "회원타입은 필수입니다.")
    private String memType;

    /** 회원 권한 */
    private String authCd;
    
    /** 부서 */
    private String department;

    /** 소셜로그인타입 */
    private String socialLoginType;

    /** 가입일시 */
    private Timestamp joinDt;
    
    /** 수정일시 */
    private Timestamp updDt;
    
    /** 수정자 */
    private String updId;
    
    /** 생성일시 */
    private Timestamp crtDt;
    
    /** 생성자 */
    private String crtId;
    
    /** 로그인세션 */
    private String loginSession;
    
    /** 토큰 */
    private String token;
    
    /** 로그인타임스탬프 */
    private Long loginTimestamp;
    
    /** 카카오 ID */
    private String kakaoId;
    
    /** 구글 ID */
    private String googleId;
    
    /** 프로필 이미지 URL */
    private String profileImageUrl;
    
    /** 생년월일 */
    private String birthDate;
    
    /** 성별 */
    private String gender;
    
    /** 주소 */
    private String address;
    
    /** 상세주소 */
    private String detailAddress;
    
    /** 우편번호 */
    private String zipCode;
    
    /** 국가코드 */
    private String countryCode;
    
    /** 언어코드 */
    private String languageCode;
    
    /** 시간대 */
    private String timezone;
    
    /** 마지막 로그인 일시 */
    private Timestamp lastLoginDt;
    
    /** 마지막 로그인 IP */
    private String lastLoginIp;
    
    /** 로그인 시도 횟수 */
    private Integer loginAttemptCount;
    
    /** 계정 잠금 일시 */
    private Timestamp accountLockedDt;
    
    /** 계정 잠금 사유 */
    private String accountLockReason;
    
    /** 비밀번호 변경 일시 */
    private Timestamp passwordChangeDt;
    
    /** 이메일 인증 여부 */
    private Boolean emailVerified;
    
    /** 휴대폰 인증 여부 */
    private Boolean mobileVerified;
    
    /** 이메일 인증 일시 */
    private Timestamp emailVerifiedDt;
    
    /** 휴대폰 인증 일시 */
    private Timestamp mobileVerifiedDt;
    
    /** 약관 동의 일시 */
    private Timestamp termsAgreedDt;
    
    /** 개인정보처리방침 동의 일시 */
    private Timestamp privacyAgreedDt;
    
    /** 마케팅 수신 동의 여부 */
    private Boolean marketingAgreed;
    
    /** 마케팅 수신 동의 일시 */
    private Timestamp marketingAgreedDt;
    
    /** 푸시 알림 동의 여부 */
    private Boolean pushNotificationAgreed;
    
    /** 푸시 알림 동의 일시 */
    private Timestamp pushNotificationAgreedDt;
    
    /** 이메일 알림 동의 여부 */
    private Boolean emailNotificationAgreed;
    
    /** 이메일 알림 동의 일시 */
    private Timestamp emailNotificationAgreedDt;
    
    /** SMS 알림 동의 여부 */
    private Boolean smsNotificationAgreed;
    
    /** SMS 알림 동의 일시 */
    private Timestamp smsNotificationAgreedDt;
    
    /** 회원 등급 */
    private String memberGrade;
    
    /** 회원 포인트 */
    private Long memberPoints;
    
    /** 회원 레벨 */
    private Integer memberLevel;
    
    /** 회원 경험치 */
    private Long memberExp;
    
    /** 회원 뱃지 */
    private String memberBadges;
    
    /** 회원 성취 */
    private String memberAchievements;
    
    /** 회원 설정 */
    private String memberSettings;
    
    /** 회원 메타데이터 */
    private String memberMetadata;
    
    /** 회원 태그 */
    private String memberTags;
    
    /** 회원 카테고리 */
    private String memberCategory;
    
    /** 회원 서브카테고리 */
    private String memberSubCategory;
    
    /** 회원 그룹 */
    private String memberGroup;
    
    /** 회원 팀 */
    private String memberTeam;
    
    /** 회원 부서 */
    private String memberDepartment;
    
    /** 회원 직책 */
    private String memberPosition;
    
    /** 회원 역할 */
    private String memberRole;
    
    /** 회원 권한 */
    private String memberPermissions;
    
    /** 회원 상태 메시지 */
    private String memberStatusMessage;
    
    /** 회원 소개 */
    private String memberIntroduction;
    
    /** 회원 웹사이트 */
    private String memberWebsite;
    
    /** 회원 소셜미디어 */
    private String memberSocialMedia;
    
    /** 회원 관심사 */
    private String memberInterests;
    
    /** 회원 취미 */
    private String memberHobbies;
    
    /** 회원 스킬 */
    private String memberSkills;
    
    /** 회원 경력 */
    private String memberCareer;
    
    /** 회원 교육 */
    private String memberEducation;
    
    /** 회원 자격증 */
    private String memberCertifications;
    
    /** 회원 수상경력 */
    private String memberAwards;
    
    /** 회원 프로젝트 */
    private String memberProjects;
    
    /** 회원 포트폴리오 */
    private String memberPortfolio;
    
    /** 회원 연락처 */
    private String memberContacts;
    
    /** 회원 비상연락처 */
    private String memberEmergencyContacts;
    
    /** 회원 의료정보 */
    private String memberMedicalInfo;
    
    /** 회원 보험정보 */
    private String memberInsuranceInfo;
    
    /** 회원 급여정보 */
    private String memberSalaryInfo;
    
    /** 회원 휴가정보 */
    private String memberVacationInfo;
    
    /** 회원 근무정보 */
    private String memberWorkInfo;
    
    /** 회원 출퇴근정보 */
    private String memberCommuteInfo;
    
    /** 회원 장비정보 */
    private String memberEquipmentInfo;
    
    /** 회원 보안정보 */
    private String memberSecurityInfo;
    
    /** 회원 감사정보 */
    private String memberAuditInfo;
    
    /** 회원 백업정보 */
    private String memberBackupInfo;
    
    /** 회원 복원정보 */
    private String memberRestoreInfo;
    
    /** 회원 마이그레이션정보 */
    private String memberMigrationInfo;
    
    /** 회원 동기화정보 */
    private String memberSyncInfo;
    
    /** 회원 분석정보 */
    private String memberAnalysisInfo;
    
    /** 회원 리포트정보 */
    private String memberReportInfo;
    
    /** 회원 압축정보 */
    private String memberCompressionInfo;
    
    /** 회원 암호화정보 */
    private String memberEncryptionInfo;
    
    /** 회원 검색정보 */
    private String memberSearchInfo;
    
    /** 회원 필터정보 */
    private String memberFilterInfo;
    
    /** 회원 정렬정보 */
    private String memberSortInfo;
    
    /** 회원 그룹정보 */
    private String memberGroupInfo;
    
    /** 회원 집계정보 */
    private String memberAggregateInfo;
    
    /** 회원 변환정보 */
    private String memberTransformInfo;
    
    /** 회원 검증정보 */
    private String memberValidationInfo;
    
    /** 회원 품질정보 */
    private String memberQualityInfo;
    
    /** 회원 모니터링정보 */
    private String memberMonitoringInfo;
    
    /** 회원 알림정보 */
    private String memberNotificationInfo;
    
    /** 회원 로깅정보 */
    private String memberLoggingInfo;
    
    /** 회원 감사정보 */
    private String memberAuditInfo2;
    
    /** 회원 보안정보 */
    private String memberSecurityInfo2;
    
    /** 회원 권한정보 */
    private String memberPermissionInfo;
    
    /** 회원 접근제어정보 */
    private String memberAccessControlInfo;
    
    /** 회원 백업스케줄정보 */
    private String memberBackupScheduleInfo;
    
    /** 회원 복원스케줄정보 */
    private String memberRestoreScheduleInfo;
    
    /** 회원 마이그레이션스케줄정보 */
    private String memberMigrationScheduleInfo;
    
    /** 회원 동기화스케줄정보 */
    private String memberSyncScheduleInfo;
    
    /** 회원 분석스케줄정보 */
    private String memberAnalysisScheduleInfo;
    
    /** 회원 리포트스케줄정보 */
    private String memberReportScheduleInfo;
    
    /** 회원 압축스케줄정보 */
    private String memberCompressionScheduleInfo;
    
    /** 회원 암호화스케줄정보 */
    private String memberEncryptionScheduleInfo;
    
    /** 회원 검색스케줄정보 */
    private String memberSearchScheduleInfo;
    
    /** 회원 필터스케줄정보 */
    private String memberFilterScheduleInfo;
    
    /** 회원 정렬스케줄정보 */
    private String memberSortScheduleInfo;
    
    /** 회원 그룹스케줄정보 */
    private String memberGroupScheduleInfo;
    
    /** 회원 집계스케줄정보 */
    private String memberAggregateScheduleInfo;
    
    /** 회원 변환스케줄정보 */
    private String memberTransformScheduleInfo;
    
    /** 회원 검증스케줄정보 */
    private String memberValidationScheduleInfo;
    
    /** 회원 품질스케줄정보 */
    private String memberQualityScheduleInfo;
    
    /** 회원 모니터링스케줄정보 */
    private String memberMonitoringScheduleInfo;
    
    /** 회원 알림스케줄정보 */
    private String memberNotificationScheduleInfo;
    
    /** 회원 로깅스케줄정보 */
    private String memberLoggingScheduleInfo;
    
    /** 회원 감사스케줄정보 */
    private String memberAuditScheduleInfo;
    
    /** 회원 보안스케줄정보 */
    private String memberSecurityScheduleInfo;
    
    /** 회원 권한스케줄정보 */
    private String memberPermissionScheduleInfo;
    
    /** 회원 접근제어스케줄정보 */
    private String memberAccessControlScheduleInfo;

}
