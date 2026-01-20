package com.zinidata.audit.enums;

/**
 * 감사 로그 액션 타입 열거형
 * 
 * <p>사용자의 행위를 단순하게 분류하여 감사 로그에 기록하기 위한 액션 타입을 정의합니다.</p>
 * 
 * <h3>💡 단순화된 가이드라인</h3>
 * <ul>
 *   <li><strong>PAGE_VIEW</strong>: 페이지 접근 (로그인 페이지, 대시보드, 조회 화면 등)</li>
 *   <li><strong>API_CALL</strong>: API 호출 (로그인, 데이터 처리, 업데이트 등)</li>
 *   <li><strong>SYSTEM</strong>: 시스템 이벤트 (세션 만료, 자동 로그아웃 등)</li>
 *   <li><strong>FILE_UPLOAD</strong>: 파일 업로드</li>
 *   <li><strong>FILE_DOWNLOAD</strong>: 파일 다운로드</li>
 *   <li><strong>REPORT_VIEW</strong>: 레포트 조회 및 데이터 추출</li>
 * </ul>
 * 
 * <h3>📋 사용 예시</h3>
 * <pre>
 * // 페이지 접근
 * PAGE_VIEW + targetResource=page:/auth/login     // 로그인 페이지 접근
 * PAGE_VIEW + targetResource=page:/dashboard      // 대시보드 접근
 * 
 * // API 호출
 * API_CALL + targetResource=api:/auth/login       // 로그인 API 호출
 * API_CALL + targetResource=api:/users/update     // 사용자 정보 수정
 * 
 * // 시스템 이벤트
 * SYSTEM + targetResource=system:session-expired  // 세션 만료
 * SYSTEM + targetResource=system:auto-logout      // 자동 로그아웃
 * 
 * // 파일 처리
 * FILE_UPLOAD + targetResource=file:/documents    // 문서 업로드
 * FILE_DOWNLOAD + targetResource=file:/reports    // 레포트 다운로드
 * 
 * // 레포트
 * REPORT_VIEW + targetResource=report:/analytics  // 분석 레포트 조회
 * </pre>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
public enum AuditActionType {
    /**
     * 페이지 접근
     * 
     * <p>모든 페이지 접근을 의미합니다. (로그인 페이지, 대시보드, 조회 화면 등)</p>
     * <p>targetResource에서 구체적인 행위를 구분합니다.</p>
     */
    PAGE_VIEW,
    
    /**
     * API 호출
     * 
     * <p>모든 API 호출을 의미합니다. (로그인, 데이터 처리, 업데이트 등)</p>
     * <p>targetResource에서 구체적인 행위를 구분합니다.</p>
     */
    API_CALL,
    
    /**
     * 시스템 이벤트
     * 
     * <p>시스템 자동 처리 이벤트를 의미합니다. (세션 만료, 자동 로그아웃 등)</p>
     */
    SYSTEM,
    
    /**
     * 파일 업로드
     * 
     * <p>파일 업로드 관련 행위를 의미합니다.</p>
     */
    FILE_UPLOAD,
    
    /**
     * 파일 다운로드
     * 
     * <p>파일 다운로드 관련 행위를 의미합니다.</p>
     */
    FILE_DOWNLOAD,
    
    /**
     * 레포트 조회
     * 
     * <p>레포트나 데이터 추출 관련 행위를 의미합니다.</p>
     */
    REPORT_VIEW,
    
    /**
     * 관리자 액션
     * 
     * <p>관리자 권한이 필요한 시스템 관리 행위를 의미합니다.</p>
     * <p>업데이트 모드 제어, 검수 환경 관리, 시스템 설정 변경 등</p>
     */
    ADMIN_ACTION
} 