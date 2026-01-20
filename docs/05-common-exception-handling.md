# ⚠️ Common Exception Handling 가이드

> **ZiniData 프로젝트 예외 처리 시스템**

## 📌 개요

이 프로젝트는 **일관된 예외 처리 방식**을 제공하기 위해 커스텀 예외 클래스와 전역 예외 핸들러를 사용합니다. 모든 예외는 자동으로 표준화된 응답 형태로 변환됩니다.

### 🎯 주요 특징
- **자동 예외 처리**: `GlobalExceptionHandler`가 모든 예외를 캐치하여 처리
- **표준 응답 형식**: 모든 예외가 `ApiResponse` 형식으로 변환
- **자동 감사 로그**: 중요한 예외는 자동으로 감사 로그 기록
- **타입별 처리**: 비즈니스 예외, 검증 예외, 시스템 예외 등 구분 처리

---

## 🏗️ 예외 처리 아키텍처

```
Controller/Service
    ↓ (예외 발생)
BusinessException / ValidationException
    ↓ (자동 전파)
GlobalExceptionHandler
    ↓ (자동 처리)
ApiResponse<T>
    ↓ (클라이언트로 전송)
표준화된 에러 응답
```

### 핵심 컴포넌트

1. **BusinessException**: 비즈니스 로직 예외
2. **ValidationException**: 유효성 검증 예외
3. **GlobalExceptionHandler**: 전역 예외 처리 핸들러
4. **Status Enum**: 예외별 상태 코드

---

## 🔧 BusinessException 사용법

### 1. 기본 사용

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public UserVO findById(Long userId) {
        UserVO user = userMapper.findById(userId);
        
        if (user == null) {
            // 데이터 없음 예외
            throw new BusinessException(Status.데이터없음);
        }
        
        return user;
    }
}
```

### 2. 커스텀 메시지 사용

```java
public UserVO login(LoginVO loginVO) {
    UserVO user = userMapper.findByLoginId(loginVO.getLoginId());
    
    if (user == null || !passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
        throw new BusinessException(Status.로그인실패, "아이디 또는 비밀번호를 확인해주세요.");
    }
    
    return user;
}
```

### 3. 추가 데이터 포함

```java
public void checkDuplicateLoginId(String loginId) {
    if (userMapper.existsByLoginId(loginId)) {
        Map<String, Object> duplicateInfo = Map.of(
            "loginId", loginId,
            "conflictType", "LOGIN_ID"
        );
        
        throw new BusinessException(Status.아이디중복, "이미 사용중인 아이디입니다.", duplicateInfo);
    }
}
```

### 4. 원인 예외 포함

```java
public void processPayment(PaymentVO payment) {
    try {
        tossPaymentService.charge(payment);
        
    } catch (TossException e) {
        throw new BusinessException(Status.결제오류, "결제 처리 중 오류가 발생했습니다.", e);
    }
}
```

---

## ✅ ValidationException 사용법

### 1. 필드별 검증 오류

```java
@Service
public class UserService {
    
    public void validateUser(UserVO user) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            fieldErrors.put("email", "올바른 이메일 형식이 아닙니다.");
        }
        
        if (user.getPassword() != null && user.getPassword().length() < 8) {
            fieldErrors.put("password", "비밀번호는 8자 이상이어야 합니다.");
        }
        
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException(Status.파라미터오류, fieldErrors);
        }
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
```

### 2. 전역 검증 오류

```java
public void validateBusinessRules(UserVO user) {
    List<String> globalErrors = new ArrayList<>();
    
    // 비즈니스 규칙 검증
    if (user.getAge() < 14) {
        globalErrors.add("만 14세 이상만 가입할 수 있습니다.");
    }
    
    if (user.getCountry() == null || !ALLOWED_COUNTRIES.contains(user.getCountry())) {
        globalErrors.add("지원하지 않는 국가입니다.");
    }
    
    if (!globalErrors.isEmpty()) {
        throw new ValidationException(Status.파라미터오류, globalErrors);
    }
}
```

### 3. 필드 및 전역 오류 동시 사용

```java
public void validateComplete(UserVO user) {
    Map<String, String> fieldErrors = new HashMap<>();
    List<String> globalErrors = new ArrayList<>();
    
    // 필드별 검증
    if (user.getEmail() == null) {
        fieldErrors.put("email", "이메일은 필수입니다.");
    }
    
    // 전역 검증
    if (user.getReferralCode() != null && !referralService.isValidCode(user.getReferralCode())) {
        globalErrors.add("유효하지 않은 추천 코드입니다.");
    }
    
    if (!fieldErrors.isEmpty() || !globalErrors.isEmpty()) {
        throw new ValidationException(Status.파라미터오류, fieldErrors, globalErrors);
    }
}
```

---

## 🎯 GlobalExceptionHandler 동작 방식

### 자동 처리되는 예외

`GlobalExceptionHandler`가 다음 예외를 자동으로 처리합니다:

1. **BusinessException**: 비즈니스 로직 예외
2. **ValidationException**: 유효성 검증 예외
3. **MethodArgumentNotValidException**: Bean Validation 예외
4. **PaymentException**: 결제 관련 예외
5. **NoResourceFoundException**: 404 에러
6. **Exception**: 기타 모든 예외 (fallback)

### 응답 형식

각 예외는 자동으로 `ApiResponse` 형식으로 변환되어 반환됩니다:

```json
{
  "success": false,
  "code": "4001",
  "message": "이미 사용중인 아이디입니다.",
  "error": {
    "type": "아이디중복",
    "details": "business.duplicate.loginId"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 💡 사용 패턴

### 1. 중복 체크 패턴

```java
@PostMapping("/register")
public ApiResponse<UserVO> register(@Valid @RequestBody UserVO user) {
    // 아이디 중복 체크
    if (userService.existsByLoginId(user.getLoginId())) {
        throw new BusinessException(Status.아이디중복);
    }
    
    // 이메일 중복 체크
    if (userService.existsByEmail(user.getEmail())) {
        throw new BusinessException(Status.이메일중복);
    }
    
    UserVO createdUser = userService.create(user);
    return ApiResponse.success(createdUser);
}
```

### 2. 존재 여부 체크 패턴

```java
@GetMapping("/users/{userId}")
public ApiResponse<UserVO> getUser(@PathVariable Long userId) {
    UserVO user = userService.findById(userId);
    
    if (user == null) {
        throw new BusinessException(Status.데이터없음);
    }
    
    return ApiResponse.success(user);
}
```

### 3. 권한 체크 패턴

```java
@DeleteMapping("/users/{userId}")
public ApiResponse<Void> deleteUser(
        @PathVariable Long userId, 
        Authentication auth) {
    
    // 권한 체크
    if (!auth.getName().equals(String.valueOf(userId))) {
        throw new BusinessException(Status.권한없음);
    }
    
    userService.delete(userId);
    return ApiResponse.success();
}
```

### 4. 비즈니스 규칙 검증 패턴

```java
@PostMapping("/payment")
public ApiResponse<PaymentVO> processPayment(@RequestBody PaymentVO payment) {
    // 결제 금액 검증
    if (payment.getAmount() <= 0) {
        throw new ValidationException(
            Status.파라미터오류,
            Map.of("amount", "결제 금액은 0보다 커야 합니다.")
        );
    }
    
    // 잔액 검증
    if (userService.getBalance(payment.getUserId()) < payment.getAmount()) {
        throw new BusinessException(Status.잔액부족, "잔액이 부족합니다.");
    }
    
    PaymentVO result = paymentService.process(payment);
    return ApiResponse.success(result);
}
```

### 5. 외부 API 호출 패턴

```java
@Service
public class KakaoAuthService {
    
    @Value("${kakao.api.key}")
    private String apiKey;
    
    public KakaoUserVO getUserInfo(String accessToken) {
        try {
            // 외부 API 호출
            return kakaoApiClient.getUserInfo(accessToken);
            
        } catch (KakaoApiException e) {
            // 외부 서비스 오류
            throw new BusinessException(
                Status.외부API오류,
                "카카오 API 호출 중 오류가 발생했습니다.",
                e
            );
        } catch (Exception e) {
            // 예상치 못한 오류
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new BusinessException(Status.시스템오류);
        }
    }
}
```

---

## ⚠️ 주의사항

### 1. 예외는 Controller에서 처리하지 말 것

```java
// ❌ 잘못된 예시
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
    try {
        UserVO createdUser = userService.create(user);
        return ApiResponse.success(createdUser);
    } catch (BusinessException e) {
        // GlobalExceptionHandler가 처리하므로 여기서 처리할 필요 없음
        return new ApiResponse<>(e.getStatus());
    }
}

// ✅ 올바른 예시
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
    // 예외를 발생시키기만 하면 GlobalExceptionHandler가 자동 처리
    UserVO createdUser = userService.create(user);
    return ApiResponse.success(createdUser);
}
```

### 2. 적절한 Status 선택

```java
// ✅ 올바른 Status 사용
throw new BusinessException(Status.아이디중복);      // 4001
throw new BusinessException(Status.이메일중복);       // 4002
throw new BusinessException(Status.휴대폰중복);       // 4003

// ❌ 잘못된 Status 사용
throw new BusinessException(Status.시스템오류);      // 9001 (시스템 오류가 아님)
throw new BusinessException(Status.로그인실패);      // 1001 (로그인 관련 아님)
```

### 3. 예외 메시지는 사용자 친화적으로

```java
// ❌ 개발자용 메시지
throw new BusinessException(Status.아이디중복, "User already exists with loginId: " + loginId);

// ✅ 사용자 친화적 메시지
throw new BusinessException(Status.아이디중복, "이미 사용중인 아이디입니다.");
```

### 4. 검증은 서비스 계층에서

```java
// ✅ 서비스 계층에서 검증
@Service
public class UserService {
    public void validateAndCreate(UserVO user) {
        // 검증 로직
        if (user.getEmail() == null) {
            throw new ValidationException(Status.파라미터오류, 
                Map.of("email", "이메일은 필수입니다."));
        }
        
        // 비즈니스 로직
        userMapper.insert(user);
    }
}

// ❌ 컨트롤러에서 검증 (비즈니스 로직은 서비스 계층에 위치해야 함)
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
    if (user.getEmail() == null) {
        throw new ValidationException(...);
    }
    // ...
}
```

---

## 🎨 커스텀 예외 만들기

### 새로운 비즈니스 예외 추가

```java
// 1. Status enum에 새로운 상태 코드 추가
public enum Status {
    // ... 기존 코드들
    
    잔액부족("4005", "business.insufficient.balance", "잔액이 부족합니다."),
    구독만료("4006", "business.expired.subscription", "구독이 만료되었습니다.");
}

// 2. BusinessException 사용
@Service
public class PaymentService {
    public void checkBalance(Long userId, Long amount) {
        Long balance = getUserBalance(userId);
        
        if (balance < amount) {
            throw new BusinessException(Status.잔액부족);
        }
    }
}
```

### 도메인별 커스텀 예외 생성

```java
// Payment 도메인 전용 예외
public class PaymentException extends BusinessException {
    
    private String errorCode;
    private String errorMessage;
    
    public PaymentException(Status status, String tossErrorCode, String tossErrorMessage) {
        super(status, "결제 처리 중 오류가 발생했습니다.");
        this.errorCode = tossErrorCode;
        this.errorMessage = tossErrorMessage;
    }
    
    // getter, setter
}

// 사용
@Service
public class PaymentService {
    public void processPayment(PaymentVO payment) {
        try {
            tossPaymentService.charge(payment);
        } catch (TossException e) {
            throw new PaymentException(
                Status.결제오류,
                e.getErrorCode(),
                e.getMessage()
            );
        }
    }
}
```

---

## 📊 예외 응답 형식

### BusinessException 응답

```json
{
  "success": false,
  "code": "4001",
  "message": "이미 사용중인 아이디입니다.",
  "error": {
    "type": "아이디중복",
    "details": "business.duplicate.loginId"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### ValidationException 응답 (필드 오류)

```json
{
  "success": false,
  "code": "3001",
  "message": "입력값을 확인해주세요.",
  "data": {
    "fieldErrors": {
      "email": "올바른 이메일 형식이 아닙니다.",
      "password": "비밀번호는 8자 이상이어야 합니다."
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### ValidationException 응답 (전역 오류)

```json
{
  "success": false,
  "code": "3001",
  "message": "가입 조건을 확인해주세요.",
  "data": {
    "globalErrors": [
      "만 14세 이상만 가입할 수 있습니다.",
      "지원하지 않는 국가입니다."
    ]
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Bean Validation 자동 응답

`@Valid` 어노테이션으로 검증 실패 시 자동으로 처리됩니다:

```json
{
  "success": false,
  "code": "3001",
  "message": "입력값 검증에 실패했습니다.",
  "data": {
    "fieldErrors": {
      "email": "이메일은 필수입니다.",
      "password": "비밀번호는 필수입니다."
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🔍 디버깅 팁

### 1. 예외 로깅 확인

`GlobalExceptionHandler`는 모든 예외를 로그로 기록합니다:

```java
// 로그 출력 예시
// Business Exception - Code: 4001, Message: 이미 사용중인 아이디입니다., URI: /api/users
```

### 2. Stack Trace 확인

개발 환경에서는 예외의 원인을 파악하기 위해 스택 트레이스를 확인할 수 있습니다.

### 3. 감사 로그 확인

중요한 예외는 자동으로 감사 로그에 기록됩니다. 데이터베이스에서 확인할 수 있습니다:

```sql
SELECT * FROM tb_audit_log 
WHERE result_status = 'FAILURE' 
ORDER BY access_time DESC 
LIMIT 10;
```

---

## 📚 참고 자료

- **클래스**: `com.zinidata.common.exception.*`
- **Status Enum**: `com.zinidata.common.enums.Status`
- **API Response**: `/docs/04-common-api-response.md`
- **Audit Log**: `/docs/02-audit-log-guide.md`

---

## 🎓 빠른 참조

### BusinessException 생성자

```java
// Status만 사용
throw new BusinessException(Status.아이디중복);

// Status + 커스텀 메시지
throw new BusinessException(Status.아이디중복, "이미 사용중인 아이디입니다.");

// Status + 추가 데이터
throw new BusinessException(Status.아이디중복, duplicateInfo);

// Status + 커스텀 메시지 + 원인 예외
throw new BusinessException(Status.결제오류, "결제 실패", e);

// 단순 메시지만 (일반 실패 상태 사용)
throw new BusinessException("시스템 오류가 발생했습니다.");
```

### ValidationException 생성자

```java
// 필드 오류만
throw new ValidationException(Status.파라미터오류, fieldErrors);

// 전역 오류만
throw new ValidationException(Status.파라미터오류, globalErrors);

// 필드 + 전역 오류
throw new ValidationException(Status.파라미터오류, fieldErrors, globalErrors);

// 커스텀 메시지 + 필드 오류
throw new ValidationException(Status.파라미터오류, "입력값 확인", fieldErrors);
```

