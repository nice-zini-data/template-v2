package com.zinidata.audit.vo;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.audit.enums.AuditResultStatus;
import com.zinidata.common.vo.BaseVO;

/**
 * 감사 로그 데이터 저장 VO
 * 
 * <p>tb_audit_log 테이블 구조에 맞춘 필드들을 포함합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
public class AuditLogVO extends BaseVO {
    
    private Long memNo;              // 회원 번호
    private String prjType;          // 프로젝트 타입
    private String clientIp;         // 클라이언트 IP
    private String requestUri;       // 요청 URI
    private String httpMethod;       // HTTP 메서드
    private String parameters;       // 요청 파라미터
    private String userAgent;        // 사용자 에이전트
    private String actionType;       // 액션 타입
    private String targetResource;   // 대상 리소스
    private String resultStatus;     // 결과 상태
    private String errorMessage;     // 에러 메시지
    private LocalDateTime accessTime; // 접근 시간
    private Long processingTime;     // 처리 시간 (밀리초)
    private String referrer;         // 참조 페이지
    private String sessionId;        // 세션 ID
    
    // ==================== 내부 처리 필드 ====================
    
    @JsonIgnore
    private String clientIpMasked;         // 마스킹된 클라이언트 IP (로그 출력용)
    
    @JsonIgnore
    private String loginFailReason;        // 로그인 실패 사유 (상세)
    
    @JsonIgnore
    private Boolean isCriticalAction;      // 중요 작업 여부
    
    // ==================== 편의 메서드 ====================
    
    /**
     * ActionType을 ENUM으로 설정
     */
    public void setActionType(AuditActionType actionType) {
        this.actionType = actionType.name();
    }
    
    /**
     * ResultStatus를 ENUM으로 설정
     */
    public void setResultStatus(AuditResultStatus resultStatus) {
        this.resultStatus = resultStatus.name();
    }
    
    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(this.resultStatus);
    }
    
    /**
     * 실패 여부 확인  
     */
    public boolean isFailure() {
        return !isSuccess();
    }
    
    /**
     * 보안 관련 실패 여부 확인
     */
    public boolean isSecurityFailure() {
        return "UNAUTHORIZED".equals(this.resultStatus) || 
               "FORBIDDEN".equals(this.resultStatus);
    }
    
    // 기본 생성자
    public AuditLogVO() {}
    
    // 생성자
    public AuditLogVO(Long memNo, String prjType, String clientIp, String requestUri, 
                      String httpMethod, String parameters, String userAgent, 
                      String actionType, String targetResource, String resultStatus) {
        this.memNo = memNo;
        this.prjType = prjType;
        this.clientIp = clientIp;
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.parameters = parameters;
        this.userAgent = userAgent;
        this.actionType = actionType;
        this.targetResource = targetResource;
        this.resultStatus = resultStatus;
        this.accessTime = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public Long getMemNo() {
        return memNo;
    }
    
    public void setMemNo(Long memNo) {
        this.memNo = memNo;
    }
    
    public String getPrjType() {
        return prjType;
    }
    
    public void setPrjType(String prjType) {
        this.prjType = prjType;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public String getRequestUri() {
        return requestUri;
    }
    
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getTargetResource() {
        return targetResource;
    }
    
    public void setTargetResource(String targetResource) {
        this.targetResource = targetResource;
    }
    
    public String getResultStatus() {
        return resultStatus;
    }
    
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getAccessTime() {
        return accessTime;
    }
    
    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }
    
    public Long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }
    
    public String getReferrer() {
        return referrer;
    }
    
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getClientIpMasked() {
        return clientIpMasked;
    }
    
    public void setClientIpMasked(String clientIpMasked) {
        this.clientIpMasked = clientIpMasked;
    }
    
    public String getLoginFailReason() {
        return loginFailReason;
    }
    
    public void setLoginFailReason(String loginFailReason) {
        this.loginFailReason = loginFailReason;
    }
    
    public Boolean getIsCriticalAction() {
        return isCriticalAction;
    }
    
    public void setIsCriticalAction(Boolean isCriticalAction) {
        this.isCriticalAction = isCriticalAction;
    }
    
    @Override
    public String toString() {
        return "AuditLogVO{" +
                "memNo=" + memNo +
                ", prjType='" + prjType + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", parameters='" + parameters + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", actionType='" + actionType + '\'' +
                ", targetResource='" + targetResource + '\'' +
                ", resultStatus='" + resultStatus + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", accessTime=" + accessTime +
                ", processingTime=" + processingTime +
                ", referrer='" + referrer + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
} 