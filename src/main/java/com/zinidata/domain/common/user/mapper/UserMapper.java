package com.zinidata.domain.common.user.mapper;

import com.zinidata.common.dto.PageRequest;
import com.zinidata.domain.common.user.vo.UserVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 사용자 관리 매퍼
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface UserMapper {
    
    // ========== 조회 기능 ==========
    
    /**
     * 사용자 ID로 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    UserVO selectUserById(@Param("userId") Long userId);
    
    /**
     * 로그인 ID로 사용자 정보 조회
     * 
     * @param loginId 로그인 ID
     * @return 사용자 정보
     */
    UserVO selectUserByLoginId(@Param("loginId") String loginId);
    
    /**
     * 이메일로 사용자 정보 조회
     * 
     * @param email 이메일
     * @return 사용자 정보
     */
    UserVO selectUserByEmail(@Param("email") String email);
    
    /**
     * 사용자 목록 조회 (페이징)
     * 
     * @param pageRequest 페이징 요청
     * @return 사용자 목록
     */
    List<UserVO> selectUserList(PageRequest pageRequest);
    
    /**
     * 사용자 총 개수 조회
     * 
     * @param pageRequest 페이징 요청 (검색 조건 포함)
     * @return 사용자 총 개수
     */
    int selectUserCount(PageRequest pageRequest);
    
    // ========== 생성/수정/삭제 기능 ==========
    
    /**
     * 사용자 등록
     * 
     * @param userVO 사용자 정보
     * @return 등록된 행 수
     */
    int insertUser(UserVO userVO);
    
    /**
     * 사용자 정보 수정
     * 
     * @param userVO 사용자 정보
     * @return 수정된 행 수
     */
    int updateUser(UserVO userVO);
    
    /**
     * 사용자 삭제 (논리적 삭제)
     * 
     * @param userId 사용자 ID
     * @param deletedBy 삭제자 ID
     * @return 삭제된 행 수
     */
    int deleteUser(@Param("userId") Long userId, @Param("deletedBy") Long deletedBy);
    
    // ========== 인증 관련 기능 ==========
    
    /**
     * 로그인 검증 (로그인 ID와 암호화된 비밀번호로 조회)
     * 
     * @param loginId 로그인 ID
     * @param encryptedPassword 암호화된 비밀번호
     * @return 사용자 정보
     */
    UserVO selectUserForLogin(@Param("loginId") String loginId, 
                             @Param("encryptedPassword") String encryptedPassword);
    
    /**
     * 비밀번호 변경
     * 
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호 (암호화된)
     * @param updatedBy 수정자 ID
     * @return 수정된 행 수
     */
    int updatePassword(@Param("userId") Long userId, 
                       @Param("newPassword") String newPassword,
                       @Param("updatedBy") Long updatedBy);
    
    /**
     * 로그인 실패 횟수 증가
     * 
     * @param userId 사용자 ID
     * @return 수정된 행 수
     */
    int incrementLoginFailCount(@Param("userId") Long userId);
    
    /**
     * 로그인 실패 횟수 초기화
     * 
     * @param userId 사용자 ID
     * @return 수정된 행 수
     */
    int resetLoginFailCount(@Param("userId") Long userId);
    
    /**
     * 계정 잠금 처리
     * 
     * @param userId 사용자 ID
     * @return 수정된 행 수
     */
    int lockAccount(@Param("userId") Long userId);
    
    /**
     * 계정 잠금 해제
     * 
     * @param userId 사용자 ID
     * @return 수정된 행 수
     */
    int unlockAccount(@Param("userId") Long userId);
    
    /**
     * 마지막 로그인 정보 업데이트
     * 
     * @param userId 사용자 ID
     * @param clientIp 클라이언트 IP
     * @param sessionId 세션 ID
     * @return 수정된 행 수
     */
    int updateLastLoginInfo(@Param("userId") Long userId, 
                           @Param("clientIp") String clientIp,
                           @Param("sessionId") String sessionId);
    
    // ========== 중복 검사 기능 ==========
    
    /**
     * 로그인 ID 중복 검사
     * 
     * @param loginId 로그인 ID
     * @param excludeUserId 제외할 사용자 ID (수정 시 자신 제외)
     * @return 중복 개수
     */
    int checkLoginIdDuplicate(@Param("loginId") String loginId, 
                             @Param("excludeUserId") Long excludeUserId);
    
    /**
     * 이메일 중복 검사
     * 
     * @param email 이메일
     * @param excludeUserId 제외할 사용자 ID (수정 시 자신 제외)
     * @return 중복 개수
     */
    int checkEmailDuplicate(@Param("email") String email, 
                           @Param("excludeUserId") Long excludeUserId);
    
    /**
     * 휴대폰 번호 중복 검사
     * 
     * @param phoneNumber 휴대폰 번호
     * @param excludeUserId 제외할 사용자 ID (수정 시 자신 제외)
     * @return 중복 개수
     */
    int checkPhoneNumberDuplicate(@Param("phoneNumber") String phoneNumber, 
                                 @Param("excludeUserId") Long excludeUserId);
    
    // ========== 세션 관리 기능 ==========
    
    /**
     * 세션 정보 업데이트
     * 
     * @param userId 사용자 ID
     * @param sessionId 세션 ID
     * @return 수정된 행 수
     */
    int updateSessionInfo(@Param("userId") Long userId, 
                         @Param("sessionId") String sessionId);
    
    /**
     * 세션 정보 삭제 (로그아웃 시)
     * 
     * @param userId 사용자 ID
     * @return 수정된 행 수
     */
    int clearSessionInfo(@Param("userId") Long userId);
    
    /**
     * 세션 ID로 사용자 정보 조회
     * 
     * @param sessionId 세션 ID
     * @return 사용자 정보
     */
    UserVO selectUserBySessionId(@Param("sessionId") String sessionId);
} 