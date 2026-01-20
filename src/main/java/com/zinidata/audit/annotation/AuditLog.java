package com.zinidata.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zinidata.audit.enums.AuditActionType;

/**
 * 감사 로그 기록 어노테이션
 * 
 * <p>메서드에 이 어노테이션을 추가하면 자동으로 감사 로그가 기록됩니다.</p>
 * 
 * <h3>기본 사용법</h3>
 * <pre>
 * {@code
 * @AuditLog(actionType = ActionType.로그인, targetResource = "사용자인증")
 * public ApiResponse<?> login(AuthVO authVO) {
 *     // 로그인 처리 로직
 * }
 * }
 * </pre>
 * 
 * <h3>고급 사용법</h3>
 * <pre>
 * {@code
 * @AuditLog(
 *     actionType = ActionType.회원가입,
 *     targetResource = "회원정보",
 *     logOnSuccess = true,
 *     logOnFailure = true,
 *     includeParameters = true,
 *     sensitiveFields = {"password", "phone", "email"}
 * )
 * public ApiResponse<?> signup(MemberVO memberVO) {
 *     // 회원가입 처리 로직
 * }
 * }
 * </pre>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    
    /**
     * 작업 유형 (필수)
     * 
     * <p>AuditActionType enum 값을 사용하여 작업 종류를 분류합니다.</p>
     * 
     * @return 액션 타입
     */
    AuditActionType actionType();
    
    /**
     * 대상 리소스/기능 명시 (필수)
     * 
     * <p>작업 대상이 되는 리소스나 기능을 명시합니다.</p>
     * <p>예: "회원정보", "로그인", "인증번호", "결제정보" 등</p>
     * 
     * @return 대상 리소스 설명
     */
    String targetResource();
    
    /**
     * 성공 시에도 로그를 기록할지 여부
     * 
     * <p>일반적으로 true로 설정하지만, 대용량 처리나 빈번한 호출이 있는 경우 false로 설정할 수 있습니다.</p>
     * 
     * @return 기본값 true (성공 시에도 기록)
     */
    boolean logOnSuccess() default true;
    
    /**
     * 실패 시에도 로그를 기록할지 여부
     * 
     * <p>보안상 중요한 작업은 항상 true로 설정하는 것을 권장합니다.</p>
     * 
     * @return 기본값 true (실패 시에도 기록)
     */
    boolean logOnFailure() default true;
    
    /**
     * 요청 파라미터를 로그에 포함할지 여부
     * 
     * <p>민감한 정보가 포함된 경우 false로 설정하세요.</p>
     * <p>단, sensitiveFields로 지정된 필드는 자동으로 마스킹됩니다.</p>
     * 
     * @return 기본값 true (파라미터 포함)
     */
    boolean includeParameters() default true;
    
    /**
     * 민감정보 마스킹 처리할 필드들
     * 
     * <p>지정된 필드는 자동으로 마스킹 처리됩니다.</p>
     * <p>기본값으로 일반적인 민감정보 필드들이 포함되어 있습니다.</p>
     * 
     * @return 마스킹할 필드명 배열
     */
    String[] sensitiveFields() default {"password", "pwd", "token", "secret", "key", "mobileNo", "phone", "email", "cardNo", "accountNo"};
    
    /**
     * 추가 설명 (선택)
     * 
     * <p>해당 작업에 대한 부가적인 설명을 기록합니다.</p>
     * <p>복잡한 비즈니스 로직이나 특별한 주의사항이 있는 경우 사용하세요.</p>
     * 
     * @return 추가 설명 문자열
     */
    String description() default "";
} 