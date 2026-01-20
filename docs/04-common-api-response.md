# 📡 Common API Response 가이드

> **ZiniData 프로젝트 표준 REST API 응답 구조**

## 📌 개요

이 프로젝트는 **일관된 REST API 응답 형식**을 제공하기 위해 `ApiResponse<T>` 클래스를 사용합니다. 모든 API는 동일한 구조로 응답하여 클라이언트가 쉽게 처리할 수 있습니다.

### 🎯 주요 특징
- **일관된 응답 구조**: 성공/실패 모두 동일한 포맷
- **타입 안전성**: 제네릭으로 다양한 데이터 타입 지원
- **자동 타임스탬프**: ISO 8601 형식으로 생성 시간 기록
- **상태 코드 체계**: Status enum으로 체계적인 에러 관리

---

## 📊 응답 구조

### 성공 응답 예시

```json
{
  "success": true,
  "code": "0000",
  "message": "요청이 성공했습니다.",
  "data": {
    "userId": 123,
    "name": "홍길동"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 실패 응답 예시

```json
{
  "success": false,
  "code": "1001",
  "message": "아이디 또는 비밀번호를 확인해주세요.",
  "error": {
    "type": "로그인실패",
    "details": "auth.login.FAIL"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 유효성 검사 실패 응답 예시

```json
{
  "success": false,
  "code": "3001",
  "message": "입력값을 확인해주세요.",
  "error": {
    "type": "VALIDATION_ERROR",
    "details": "Validation failed",
    "validationErrors": [
      "이메일 형식이 올바르지 않습니다",
      "비밀번호는 8자 이상이어야 합니다"
    ]
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🔧 기본 사용법

### 1. 컨트롤러에서 직접 사용

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{userId}")
    public ApiResponse<UserVO> getUser(@PathVariable Long userId) {
        UserVO user = userService.findById(userId);
        return ApiResponse.success(user);
    }
    
    @PostMapping
    public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
        UserVO createdUser = userService.create(user);
        return ApiResponse.success(createdUser, "사용자가 생성되었습니다.");
    }
}
```

### 2. 데이터 없이 성공 응답

```java
@DeleteMapping("/{userId}")
public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
    userService.delete(userId);
    return ApiResponse.success();
}
```

### 3. Status enum 사용

```java
@GetMapping("/{userId}")
public ApiResponse<UserVO> getUser(@PathVariable Long userId) {
    try {
        UserVO user = userService.findById(userId);
        
        // Status enum으로 응답 설정
        return new ApiResponse<>(Status.성공, user);
        
    } catch (NotFoundException e) {
        // Status enum으로 에러 응답
        return new ApiResponse<>(Status.데이터없음);
    }
}
```

---

## ❌ 에러 응답 사용법

### 1. 간단한 에러 응답

```java
@PostMapping("/login")
public ApiResponse<TokenVO> login(@RequestBody LoginVO loginVO) {
    // 로그인 실패 시
    if (!authService.validate(loginVO)) {
        return ApiResponse.error("로그인에 실패했습니다.");
    }
    
    // 성공 시
    TokenVO token = authService.login(loginVO);
    return ApiResponse.success(token);
}
```

### 2. 커스텀 에러 응답

```java
@GetMapping("/users/{userId}")
public ApiResponse<UserVO> getUser(@PathVariable Long userId) {
    try {
        UserVO user = userService.findById(userId);
        return ApiResponse.success(user);
        
    } catch (NotFoundException e) {
        return ApiResponse.error(
            "2001",                              // 커스텀 에러 코드
            "사용자를 찾을 수 없습니다.",         // 사용자 메시지
            "NOT_FOUND",                         // 에러 타입
            "User with id " + userId + " not found"  // 개발자용 상세 정보
        );
    }
}
```

### 3. 인증 실패 응답

```java
@GetMapping("/profile")
public ApiResponse<ProfileVO> getProfile(Authentication auth) {
    if (auth == null) {
        return ApiResponse.unauthorized("로그인이 필요합니다.");
    }
    
    ProfileVO profile = profileService.getProfile(auth.getName());
    return ApiResponse.success(profile);
}
```

### 4. 잘못된 요청 응답

```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@Valid @RequestBody UserVO user) {
    // Bean Validation 실패 시 GlobalExceptionHandler가 자동 처리
    // 수동으로 처리하려면:
    if (user.getEmail() == null || !user.getEmail().contains("@")) {
        return ApiResponse.badRequest("이메일 형식이 올바르지 않습니다.");
    }
    
    UserVO createdUser = userService.create(user);
    return ApiResponse.success(createdUser);
}
```

### 5. 유효성 검사 실패 응답

```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
    List<String> errors = new ArrayList<>();
    
    if (user.getEmail() == null) {
        errors.add("이메일은 필수입니다.");
    }
    if (user.getPassword() != null && user.getPassword().length() < 8) {
        errors.add("비밀번호는 8자 이상이어야 합니다.");
    }
    
    if (!errors.isEmpty()) {
        return ApiResponse.validationError("입력값을 확인해주세요.", errors);
    }
    
    UserVO createdUser = userService.create(user);
    return ApiResponse.success(createdUser);
}
```

---

## 📋 Status Enum 사용법

`Status` enum은 프로젝트 전체에서 사용하는 표준 상태 코드를 제공합니다.

### 기본 상태 코드

```java
// 성공
Status.성공        // "0000" - 성공
Status.실패        // "9999" - 일반 실패

// 인증/보안 (1000번대)
Status.로그인실패   // "1001" - 로그인 실패

// 데이터 처리 (2000번대)
Status.데이터없음   // "2001" - 데이터를 찾을 수 없음

// 파라미터/검증 (3000번대)
Status.파라미터오류  // "3001" - 파라미터 오류

// 비즈니스 로직 (4000번대)
Status.아이디중복   // "4001" - 아이디 중복
Status.이메일중복   // "4002" - 이메일 중복
Status.휴대폰중복   // "4003" - 휴대폰 중복
Status.카카오중복   // "4004" - 카카오 계정 중복

// 외부 연동 (5000번대)
Status.결제오류    // "5001" - 결제 처리 오류

// 시스템 오류 (9000번대)
Status.시스템오류   // "9001" - 시스템 오류
```

### Status 사용 예시

```java
@PostMapping("/register")
public ApiResponse<UserVO> register(@RequestBody UserVO user) {
    try {
        // 아이디 중복 체크
        if (userService.existsByLoginId(user.getLoginId())) {
            return new ApiResponse<>(Status.아이디중복);
        }
        
        // 이메일 중복 체크
        if (userService.existsByEmail(user.getEmail())) {
            return new ApiResponse<>(Status.이메일중복);
        }
        
        UserVO createdUser = userService.create(user);
        return new ApiResponse<>(Status.성공, createdUser);
        
    } catch (Exception e) {
        return new ApiResponse<>(Status.시스템오류);
    }
}
```

### Status 편의 메서드

```java
// 성공 여부 확인
if (status.isSuccess()) {
    // 성공 처리
}

// 실패 여부 확인
if (status.isFail()) {
    // 실패 처리
}

// 인증 관련 오류 확인
if (status.isAuthError()) {
    // 인증 오류 처리
}

// 시스템 오류 확인
if (status.isSystemError()) {
    // 시스템 오류 처리
}

// 코드로 Status 찾기
Status status = Status.findByCode("1001");
```

---

## 💡 사용 패턴 및 베스트 프랙티스

### 1. 기본 CRUD 패턴

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // 조회: 데이터 반환
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.findById(id);
        if (user == null) {
            return new ApiResponse<>(Status.데이터없음);
        }
        return ApiResponse.success(user);
    }
    
    // 생성: 생성된 데이터 반환
    @PostMapping
    public ApiResponse<UserVO> createUser(@Valid @RequestBody UserVO user) {
        UserVO createdUser = userService.create(user);
        return ApiResponse.success(createdUser, "사용자가 생성되었습니다.");
    }
    
    // 수정: 수정된 데이터 반환
    @PutMapping("/{id}")
    public ApiResponse<UserVO> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UserVO user) {
        UserVO updatedUser = userService.update(id, user);
        return ApiResponse.success(updatedUser);
    }
    
    // 삭제: 데이터 없이 성공 응답
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }
}
```

### 2. 복잡한 데이터 반환

```java
// 리스트 반환
@GetMapping
public ApiResponse<List<UserVO>> getUserList() {
    List<UserVO> users = userService.findAll();
    return ApiResponse.success(users);
}

// 단일 객체 반환
@GetMapping("/profile")
public ApiResponse<ProfileVO> getProfile(Authentication auth) {
    ProfileVO profile = profileService.getProfile(auth.getName());
    return ApiResponse.success(profile);
}

// Map 반환
@GetMapping("/stats")
public ApiResponse<Map<String, Object>> getStats() {
    Map<String, Object> stats = Map.of(
        "totalUsers", 1000,
        "activeUsers", 850,
        "newUsers", 50
    );
    return ApiResponse.success(stats);
}
```

### 3. 에러 처리 패턴

```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
    try {
        // 비즈니스 로직 검증
        if (userService.existsByLoginId(user.getLoginId())) {
            return new ApiResponse<>(Status.아이디중복);
        }
        
        // 중복 체크: 이메일
        if (userService.existsByEmail(user.getEmail())) {
            return new ApiResponse<>(Status.이메일중복);
        }
        
        // 저장
        UserVO createdUser = userService.create(user);
        return ApiResponse.success(createdUser);
        
    } catch (BusinessException e) {
        // 비즈니스 로직 예외
        return new ApiResponse<>(e.getStatus(), e.getMessage());
        
    } catch (Exception e) {
        // 예상치 못한 예외
        log.error("사용자 생성 실패", e);
        return new ApiResponse<>(Status.시스템오류);
    }
}
```

### 4. 커스텀 메시지 사용

```java
// 성공 시 커스텀 메시지
@PostMapping("/reset-password")
public ApiResponse<Void> resetPassword(@RequestBody PasswordResetVO resetVO) {
    userService.resetPassword(resetVO);
    return ApiResponse.success(null, "비밀번호가 재설정되었습니다.");
}

// Status + 커스텀 메시지
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO user) {
    if (userService.existsByLoginId(user.getLoginId())) {
        return new ApiResponse<>(
            Status.아이디중복, 
            user.getLoginId() + "는 이미 사용중인 아이디입니다."
        );
    }
    
    UserVO createdUser = userService.create(user);
    return ApiResponse.success(createdUser);
}
```

---

## ⚠️ 주의사항

### 1. null 값 처리

```java
// ❌ 잘못된 예시
ApiResponse<String> response = ApiResponse.success(null);
response.getMessage();  // "요청이 성공했습니다."

// ✅ 올바른 예시
ApiResponse<Void> response = ApiResponse.success();
```

### 2. 제네릭 타입 명시

```java
// ❌ 타입 추론 실패
return ApiResponse.success();  // Void 타입 추론

// ✅ 명시적 타입 지정
return ApiResponse.<Void>success();
return ApiResponse.<UserVO>success(user);
```

### 3. 에러 코드 체계 준수

프로젝트의 에러 코드 체계를 따르세요:
- 0000: 성공
- 1000번대: 인증/보안
- 2000번대: 데이터 처리
- 3000번대: 검증/파라미터
- 4000번대: 비즈니스 로직
- 5000번대: 외부 연동
- 9000번대: 시스템 오류

### 4. 메시지 작성 원칙

```java
// ✅ 사용자 친화적 메시지
return ApiResponse.error("로그인이 필요합니다.");

// ❌ 개발자용 기술 메시지
return ApiResponse.error("HTTP 401 Unauthorized");

// ✅ 명확한 안내
return ApiResponse.error("비밀번호는 8자 이상이어야 합니다.");

// ❌ 모호한 메시지
return ApiResponse.error("오류가 발생했습니다.");
```

---

## 📚 참고 자료

- **클래스**: `com.zinidata.common.dto.ApiResponse`
- **Status Enum**: `com.zinidata.common.enums.Status`
- **예외 처리**: `/docs/05-common-exception-handling.md`
- **관련 문서**: `/docs/01-project-structure-guide.md`

---

## 🎓 빠른 참조

### 정적 팩토리 메서드

```java
// 성공
ApiResponse.success(data)
ApiResponse.success()
ApiResponse.success(data, message)

// 에러
ApiResponse.error(message)
ApiResponse.error(code, message, errorType, errorDetails)
ApiResponse.unauthorized(message)
ApiResponse.badRequest(message)
ApiResponse.validationError(message, errors)
```

### 생성자 사용

```java
// Status만 사용
new ApiResponse<>(Status.성공)

// Status + 데이터
new ApiResponse<>(Status.성공, data)

// Status + 메시지
new ApiResponse<>(Status.성공, message)

// Status + 데이터 + 메시지
new ApiResponse<>(Status.성공, data, message)
```

