package com.zinidata.audit.enums;

public enum AuditResultStatus {
    SUCCESS,         // 성공
    FAILURE,         // 실패
    UNAUTHORIZED,    // 인증 실패
    NOT_FOUND,       // 리소스 없음
    EXPIRED,         // 세션 만료
    FORCED           // 강제 로그아웃 (다른 곳에서 로그인)
} 