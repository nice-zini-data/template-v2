# 🔐 Audit Log 가이드

> **ZiniData 감사 로그 시스템 사용 가이드**

## 📌 개요

Audit Log 모듈은 시스템의 모든 주요 작업(로그인, 데이터 수정, 파일 업로드 등)을 자동으로 기록하여 보안 감사 및 모니터링을 제공합니다.

### 🎯 주요 기능
- **자동 로그 기록**: `@AuditLog` 어노테이션으로 간편한 로그 생성
- **민감정보 마스킹**: 비밀번호, 전화번호 등 자동 마스킹 처리
- **비동기 처리**: 성능 영향 최소화를 위한 비동기 저장
- **상세 정보 수집**: IP, User-Agent, 파라미터, 처리 시간 등 자동 수집

---

## 🏗️ 아키텍처

```
@AuditLog Annotation
    ↓
AuditLogAspect (AOP)
    ↓
AuditLogService
    ↓
AuditLogMapper (MyBatis)
    ↓
tb_audit_log Table
```

### 핵심 컴포넌트

1. **@AuditLog** (annotation)
   - 메서드에 추가하여 로그 기록 활성화
   - 액션 타입, 대상 리소스, 민감정보 필드 설정

2. **AuditLogAspect** (aspect)
   - AOP로 메서드 실행 전후 자동 처리
   - 성공/실패 여부, 처리 시간 측정

3. **AuditLogService** (service)
   - 로그 생성 및 저장 담당
   - 민감정보 마스킹 처리
   - 비동기/동기 저장 선택

4. **AuditLogMapper** (mapper)
   - DB 저장 및 조회 쿼리
   - 보안/성능/장애 모니터링 메서드 제공

5. **AuditActionType** (enum)
   - 액션 타입 정의
   - PAGE_VIEW, API_CALL, FILE_UPLOAD 등

6. **AuditResultStatus** (enum)
   - 결과 상태 정의
   - SUCCESS, FAILURE, UNAUTHORIZED 등

---

## 📝 사용법

### 기본 사용

```java
@AuditLog(
    actionType = AuditActionType.API_CALL,
    targetResource = "api:/auth/login"
)
public ApiResponse<?> login(AuthVO authVO) {
    // 로그인 처리 로직
    return ApiResponse.success();
}
```

### 파라미터 설정

```java
@AuditLog(
    actionType = AuditActionType.API_CALL,
    targetResource = "api:/users/update",
    includeParameters = true,  // 파라미터 포함 여부
    sensitiveFields = {"password", "phone", "cardNo"}  // 마스킹할 필드
)
public ApiResponse<?> updateUser(@RequestBody UserVO userVO) {
    // 사용자 정보 수정 로직
    return ApiResponse.success();
}
```

### 성공/실패 로그 제어

```java
@AuditLog(
    actionType = AuditActionType.API_CALL,
    targetResource = "api:/data/export",
    logOnSuccess = true,   // 성공 시 로그 기록
    logOnFailure = true    // 실패 시 로그 기록
)
public ApiResponse<?> exportData() {
    // 데이터 내보내기 로직
    return ApiResponse.success();
}
```

### 파일 업로드

```java
@AuditLog(
    actionType = AuditActionType.FILE_UPLOAD,
    targetResource = "file:/documents"
)
public ApiResponse<?> uploadFile(@RequestParam MultipartFile file) {
    // 파일 업로드 로직
    return ApiResponse.success();
}
```

### 페이지 접근

```java
@AuditLog(
    actionType = AuditActionType.PAGE_VIEW,
    targetResource = "page:/dashboard"
)
public String dashboard(Model model) {
    // 대시보드 표시 로직
    return "dashboard";
}
```

---

## 🔧 액션 타입 종류

### AuditActionType

| 타입 | 설명 | 사용 예시 |
|------|------|-----------|
| `PAGE_VIEW` | 페이지 접근 | 로그인 페이지, 대시보드 |
| `API_CALL` | API 호출 | 로그인, 데이터 처리, 업데이트 |
| `FILE_UPLOAD` | 파일 업로드 | 문서/이미지 업로드 |
| `FILE_DOWNLOAD` | 파일 다운로드 | 레포트 다운로드 |
| `REPORT_VIEW` | 레포트 조회 | 분석 데이터 조회 |
| `SYSTEM` | 시스템 이벤트 | 세션 만료, 자동 로그아웃 |
| `ADMIN_ACTION` | 관리자 액션 | 시스템 설정 변경 |

### AuditResultStatus

| 타입 | 설명 |
|------|------|
| `SUCCESS` | 성공 |
| `FAILURE` | 실패 |
| `UNAUTHORIZED` | 인증 실패 |
| `NOT_FOUND` | 리소스 없음 |
| `EXPIRED` | 세션 만료 |
| `FORCED` | 강제 로그아웃 |

---

## 🎨 어노테이션 속성

```java
@AuditLog(
    // [필수] 액션 타입
    actionType = AuditActionType.API_CALL,
    
    // [필수] 대상 리소스 (예: "api:/auth/login", "page:/dashboard")
    targetResource = "api:/users/update",
    
    // 성공 시 로그 기록 여부 (기본값: true)
    logOnSuccess = true,
    
    // 실패 시 로그 기록 여부 (기본값: true)
    logOnFailure = true,
    
    // 파라미터 포함 여부 (기본값: true)
    includeParameters = true,
    
    // 민감정보 필드 (기본값: password, pwd, token, secret 등)
    sensitiveFields = {"password", "phone", "email"},
    
    // 추가 설명
    description = "회원정보 수정"
)
```

---

## 📊 저장되는 정보

다음 정보들이 `tb_audit_log` 테이블에 자동 저장됩니다:

- 회원 번호 (`memNo`)
- 프로젝트 타입 (`prjType`)
- 클라이언트 IP (`clientIp`)
- 요청 URI (`requestUri`)
- HTTP 메서드 (`httpMethod`)
- 요청 파라미터 (`parameters`) - 민감정보는 마스킹됨
- User-Agent (`userAgent`)
- 액션 타입 (`actionType`)
- 대상 리소스 (`targetResource`)
- 결과 상태 (`resultStatus`)
- 에러 메시지 (`errorMessage`)
- 접근 시간 (`accessTime`)
- 처리 시간 (`processingTime`)
- 참조 페이지 (`referrer`)
- 세션 ID (`sessionId`)

---

## 🔍 조회 메서드

`AuditLogMapper`에서 제공하는 주요 조회 메서드:

### 기본 조회
```java
// 특정 회원의 감사 로그 조회
List<AuditLogVO> selectAuditLogsByMemNo(Long memNo, LocalDateTime startDate, LocalDateTime endDate, Integer limit);

// 특정 액션 타입의 감사 로그 조회
List<AuditLogVO> selectAuditLogsByActionType(String actionType, LocalDateTime startDate, LocalDateTime endDate, Integer limit);
```

### 보안 모니터링
```java
// 로그인 실패 횟수 조회 (브루트 포스 탐지)
int countFailedLoginAttempts(String clientIp, Integer minutes);

// 무단 액세스 시도 조회
List<AuditLogVO> selectUnauthorizedAttempts(LocalDateTime startDate, LocalDateTime endDate, Integer limit);
```

### 성능 모니터링
```java
// 느린 요청 조회
List<AuditLogVO> selectSlowRequests(Long thresholdMs, LocalDateTime startDate, LocalDateTime endDate, Integer limit);

// 액션 타입별 평균 처리 시간
List<AuditLogVO> selectAverageProcessingTimeByActionType(LocalDateTime startDate, LocalDateTime endDate);
```

### 장애 모니터링
```java
// 에러 로그 조회
List<AuditLogVO> selectErrorLogs(LocalDateTime startDate, LocalDateTime endDate, Integer limit);

// 특정 URI의 에러 발생 횟수
int countErrorsByRequestUri(String requestUri, LocalDateTime startDate, LocalDateTime endDate);
```

---

## ⚙️ 설정

### application.yml

```yaml
app:
  code: NBZM  # 프로젝트 타입 (tb_audit_log.prjType에 저장됨)

# 비동기 처리를 위한 Executor 설정
spring:
  task:
    execution:
      pool:
        audit-log-executor:  # AuditLogService에서 사용
          core-size: 5
          max-size: 10
          queue-capacity: 100
```

---

## 💡 모범 사례

### 1. 적절한 액션 타입 사용
```java
// 좋은 예
@AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/auth/login")

// 나쁜 예 (모호함)
@AuditLog(actionType = AuditActionType.API_CALL, targetResource = "login")
```

### 2. 민감정보는 반드시 마스킹
```java
@AuditLog(
    actionType = AuditActionType.API_CALL,
    targetResource = "api:/users/update",
    sensitiveFields = {"password", "phone", "cardNo"}
)
```

### 3. 불필요한 파라미터 제외
```java
// 대용량 파일 업로드
@AuditLog(
    actionType = AuditActionType.FILE_UPLOAD,
    targetResource = "file:/documents",
    includeParameters = false  // 파일 내용은 기록하지 않음
)
```

### 4. 중요한 작업은 항상 로깅
```java
// 회원 탈퇴, 계정 잠금 등
@AuditLog(
    actionType = AuditActionType.API_CALL,
    targetResource = "api:/users/delete",
    logOnSuccess = true,
    logOnFailure = true,
    description = "회원 탈퇴 처리"
)
```

---

## ⚠️ 주의사항

1. **웹 컨텍스트 외부**: HTTP 요청 정보가 없는 경우(스케줄러, 비동기 작업 등) 로그가 기록되지 않습니다.
2. **정적 리소스**: CSS, JS, 이미지 등은 자동으로 제외됩니다.
3. **성능**: 비동기로 처리되지만, 대용량 요청 시 주의가 필요합니다.
4. **민감정보**: `sensitiveFields`에 명시하지 않으면 마스킹되지 않습니다.
5. **트랜잭션**: 비동기 저장은 별도 트랜잭션에서 실행되므로 예외가 발생해도 원래 작업은 롤백되지 않습니다.

---

## 📚 참고 자료

- **패키지 경로**: `com.zinidata.audit`
- **테이블명**: `tb_audit_log`
- **관련 문서**: `/docs/01-project-structure-guide.md`

