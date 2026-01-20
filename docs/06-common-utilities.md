# 🛠️ Common Utilities 가이드

> **ZiniData 프로젝트 공통 유틸리티 및 기타 컴포넌트**

## 📌 개요

이 문서는 프로젝트에서 공통으로 사용되는 유틸리티, 열거형(Enum), 그리고 베이스 클래스를 설명합니다.

### 🎯 주요 내용
- **JWT 토큰 처리**: `JwtTokenUtil`
- **쿠키 관리**: `CookieUtil`
- **암호화**: `SecureHashAlgorithm`
- **상태 코드**: `Status` Enum
- **권한 관리**: `UserRole` Enum
- **베이스 VO**: `BaseVO`

---

## 🔐 JWT 토큰 처리 (JwtTokenUtil)

### 개요

A 서버(nicebizmap)에서 B 서버(BetterBoss)로 사용자 정보를 전달하기 위한 JWT 토큰을 생성합니다.

### 주요 메서드

#### 1. 일반 로그인 토큰 생성

```java
@Autowired
private JwtTokenUtil jwtTokenUtil;

@PostMapping("/login")
public ApiResponse<TokenVO> login(@RequestBody LoginVO loginVO) {
    UserVO user = authService.validateLogin(loginVO);
    
    // 일반 로그인용 JWT 토큰 생성
    String jwtToken = jwtTokenUtil.generateNormalLoginToken(user);
    
    // 쿠키에 토큰 설정
    cookieUtil.setAuthTokenCookie(response, jwtToken);
    
    return ApiResponse.success(new TokenVO(jwtToken));
}
```

#### 2. 카카오 로그인 토큰 생성

```java
@PostMapping("/kakao/login")
public ApiResponse<TokenVO> kakaoLogin(@RequestBody KakaoUserVO kakaoUser) {
    UserVO user = authService.findOrCreateKakaoUser(kakaoUser);
    
    // 카카오 로그인용 JWT 토큰 생성
    String jwtToken = jwtTokenUtil.generateKakaoLoginToken(user);
    
    // 쿠키에 토큰 설정
    cookieUtil.setAuthTokenCookie(response, jwtToken);
    
    return ApiResponse.success(new TokenVO(jwtToken));
}
```

#### 3. 토큰 검증 (테스트용)

```java
@PostMapping("/test/validate")
public ApiResponse<Map<String, String>> validateToken(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    
    boolean isValid = jwtTokenUtil.validateToken(token);
    String userId = jwtTokenUtil.getUserIdFromToken(token);
    String loginType = jwtTokenUtil.getLoginTypeFromToken(token);
    
    Map<String, String> result = Map.of(
        "valid", String.valueOf(isValid),
        "userId", userId != null ? userId : "N/A",
        "loginType", loginType != null ? loginType : "N/A"
    );
    
    return ApiResponse.success(result);
}
```

### JWT 토큰 구조

생성된 JWT 토큰에는 다음과 같은 정보가 포함됩니다:

```json
{
  "sub": "user123",
  "iss": "nicebizmap.co.kr",
  "aud": "ai.nicebizmap.co.kr",
  "user_id": "user123",
  "user_nm": "홍길동",
  "user_no": 12345,
  "user_type": "person",
  "mem_type": "person",
  "email_addr": "user@example.com",
  "login_type": "NORMAL",
  "domain": "nicebizmap.co.kr",
  "target_url": "https://ai.nicebizmap.co.kr",
  "exp": 1234567890
}
```

---

## 🍪 쿠키 관리 (CookieUtil)

### 개요

도메인 간 JWT 토큰 전달을 위한 쿠키를 관리합니다. `.nicebizmap.co.kr` 도메인으로 설정되어 A, B 서버 간 공유됩니다.

### 주요 메서드

#### 1. 인증 토큰 쿠키 설정

```java
@Autowired
private CookieUtil cookieUtil;

@PostMapping("/login")
public ApiResponse<TokenVO> login(@RequestBody LoginVO loginVO, HttpServletResponse response) {
    UserVO user = authService.validateLogin(loginVO);
    String jwtToken = jwtTokenUtil.generateNormalLoginToken(user);
    
    // 쿠키에 토큰 설정
    cookieUtil.setAuthTokenCookie(response, jwtToken);
    
    return ApiResponse.success();
}
```

#### 2. 인증 토큰 쿠키 제거 (로그아웃)

```java
@PostMapping("/logout")
public ApiResponse<Void> logout(HttpServletResponse response) {
    // 쿠키 제거
    cookieUtil.removeAuthTokenCookie(response);
    
    return ApiResponse.success();
}
```

#### 3. 개발 환경용 쿠키 설정

```java
@PostMapping("/login")
public ApiResponse<TokenVO> login(@RequestBody LoginVO loginVO, HttpServletResponse response) {
    UserVO user = authService.validateLogin(loginVO);
    String jwtToken = jwtTokenUtil.generateNormalLoginToken(user);
    
    // 개발 환경 (HTTP)용 쿠키 설정
    cookieUtil.setAuthTokenCookieForDevelopment(response, jwtToken);
    
    return ApiResponse.success();
}
```

### 쿠키 설정 정보

- **이름**: `AUTH_TOKEN`
- **도메인**: `nicebizmap.co.kr`
- **경로**: `/`
- **만료 시간**: 24시간
- **HttpOnly**: false (JavaScript 접근 가능)
- **Secure**: true (HTTPS에서만 전송)

---

## 🔒 암호화 (SecureHashAlgorithm)

### 개요

비밀번호 암호화를 위한 SHA-256 해시 알고리즘을 제공합니다.

### 주요 메서드

#### 1. 비밀번호 암호화

```java
@Service
public class UserService {
    
    public void createUser(UserVO user) {
        try {
            // 비밀번호 SHA-256 암호화
            String encryptedPassword = SecureHashAlgorithm.encryptSHA256(user.getPassword());
            user.setPassword(encryptedPassword);
            
            userMapper.insert(user);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(Status.시스템오류);
        }
    }
}
```

#### 2. 로그인 시 비밀번호 검증

```java
@PostMapping("/login")
public ApiResponse<TokenVO> login(@RequestBody LoginVO loginVO) {
    UserVO user = userMapper.findByLoginId(loginVO.getLoginId());
    
    if (user == null) {
        throw new BusinessException(Status.로그인실패);
    }
    
    try {
        // 입력된 비밀번호 암호화
        String encryptedInputPassword = SecureHashAlgorithm.encryptSHA256(loginVO.getPassword());
        
        // 저장된 비밀번호와 비교
        if (!user.getPassword().equals(encryptedInputPassword)) {
            throw new BusinessException(Status.로그인실패);
        }
        
        // 로그인 성공
        String jwtToken = jwtTokenUtil.generateNormalLoginToken(user);
        cookieUtil.setAuthTokenCookie(response, jwtToken);
        
        return ApiResponse.success(new TokenVO(jwtToken));
        
    } catch (NoSuchAlgorithmException e) {
        throw new BusinessException(Status.시스템오류);
    }
}
```

---

## 📊 Status Enum

### 개요

프로젝트 전체에서 사용하는 표준 상태 코드를 관리합니다.

### 주요 상태 코드

```java
// 성공/실패 (0000번대)
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

### 편의 메서드

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

### 사용 예시

```java
@PostMapping("/register")
public ApiResponse<UserVO> register(@RequestBody UserVO user) {
    // 아이디 중복 체크
    if (userService.existsByLoginId(user.getLoginId())) {
        throw new BusinessException(Status.아이디중복);
    }
    
    UserVO createdUser = userService.create(user);
    return new ApiResponse<>(Status.성공, createdUser);
}
```

---

## 👤 UserRole Enum

### 개요

계층적 권한 시스템을 구현합니다. 높은 레벨의 권한은 낮은 레벨의 권한을 포함합니다.

### 권한 레벨

```java
SUPER_ADMIN  // 레벨 1000 - 최고관리자 (시스템 관리)
ADMIN        // 레벨 800  - 관리자 (사이트 관리)
MANAGER      // 레벨 600  - 매니저 (일반 회원 관리)
USER         // 레벨 400  - 일반사용자 (기본 서비스 이용)
GUEST        // 레벨 200  - 게스트 (비회원)
```

### 권한 확인 메서드

```java
@GetMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<DashboardVO> getDashboard(Authentication auth) {
    // 현재 사용자 권한 확인
    UserRole userRole = UserRole.fromAuthority(
        auth.getAuthorities().iterator().next().getAuthority()
    );
    
    // 권한별 다른 데이터 제공
    if (userRole.isSuperAdmin()) {
        // 시스템 관리 데이터
        return ApiResponse.success(systemAdminService.getSystemDashboard());
    } else if (userRole.isAdmin()) {
        // 사이트 관리 데이터
        return ApiResponse.success(siteAdminService.getSiteDashboard());
    } else {
        // 제한된 데이터
        return ApiResponse.success(basicAdminService.getBasicDashboard());
    }
}
```

### 편의 메서드

```java
// 권한 확인
userRole.hasAuthority(UserRole.ADMIN)        // ADMIN 이상 권한
userRole.isSuperAdmin()                      // SUPER_ADMIN만
userRole.isAdmin()                           // ADMIN 이상
userRole.isManager()                         // MANAGER 이상
userRole.isUser()                            // USER 이상
userRole.isGuest()                           // GUEST 이상 (모든 사용자)

// 권한별 기능 접근
userRole.canManageUserApproval()             // MANAGER 이상
userRole.canManageSite()                     // ADMIN 이상
userRole.canManageSystem()                  // SUPER_ADMIN만

// 이름으로 권한 찾기
UserRole role = UserRole.fromName("ADMIN");
UserRole role = UserRole.fromAuthority("ROLE_ADMIN");
```

### Spring Security 연동

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasAnyAuthority(
                    UserRole.ADMIN.getAuthority(),
                    UserRole.SUPER_ADMIN.getAuthority()
                )
                .requestMatchers("/manager/**").hasAnyAuthority(
                    UserRole.MANAGER.getAuthority(),
                    UserRole.ADMIN.getAuthority(),
                    UserRole.SUPER_ADMIN.getAuthority()
                )
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
```

---

## 📦 BaseVO 클래스

### 개요

모든 VO의 기본 클래스입니다. 생성일시, 수정일시, 생성자, 수정자 등 공통 필드를 포함합니다.

### 주요 필드

```java
public class BaseVO {
    private LocalDateTime createdAt;    // 생성일시
    private LocalDateTime updatedAt;    // 수정일시
    private Long createdBy;             // 생성자 ID
    private Long updatedBy;             // 수정자 ID
    private Boolean deleted;            // 삭제 여부
    private LocalDateTime deletedAt;    // 삭제일시
    private Long deletedBy;             // 삭제자 ID
    private Long version;               // 버전 (낙관적 잠금)
}
```

### 사용 예시

#### 1. VO 클래스 정의

```java
@Getter
@Setter
public class UserVO extends BaseVO {
    private Long userId;
    private String loginId;
    private String password;
    private String name;
    private String email;
    // ... 기타 필드
}
```

#### 2. 생성 시 정보 설정

```java
@Service
public class UserService {
    
    public UserVO createUser(UserVO user, Authentication auth) {
        Long currentUserId = getCurrentUserId(auth);
        
        // 생성 정보 자동 설정
        user.setCreatedInfo(currentUserId);
        
        userMapper.insert(user);
        return user;
    }
}
```

#### 3. 수정 시 정보 설정

```java
@Service
public class UserService {
    
    public UserVO updateUser(Long userId, UserVO user, Authentication auth) {
        UserVO existingUser = userMapper.findById(userId);
        
        if (existingUser == null) {
            throw new BusinessException(Status.데이터없음);
        }
        
        // 수정 정보 자동 설정
        user.setUpdatedInfo(getCurrentUserId(auth));
        
        userMapper.update(user);
        return user;
    }
}
```

#### 4. 삭제 시 정보 설정

```java
@Service
public class UserService {
    
    public void deleteUser(Long userId, Authentication auth) {
        UserVO user = userMapper.findById(userId);
        
        if (user == null) {
            throw new BusinessException(Status.데이터없음);
        }
        
        // 논리적 삭제 (실제 삭제하지 않음)
        user.setDeletedInfo(getCurrentUserId(auth));
        
        userMapper.update(user);
    }
}
```

#### 5. 삭제 여부 확인

```java
@Service
public class UserService {
    
    public List<UserVO> findAllActive() {
        List<UserVO> allUsers = userMapper.findAll();
        
        // 삭제되지 않은 사용자만 필터링
        return allUsers.stream()
            .filter(user -> !user.isDeleted())  // BaseVO의 isDeleted() 메서드 사용
            .collect(Collectors.toList());
    }
}
```

### 편의 메서드

```java
// 생성 정보 설정
user.setCreatedInfo(createdBy);    // createdAt, createdBy, deleted=false, version=1L 설정

// 수정 정보 설정
user.setUpdatedInfo(updatedBy);    // updatedAt, updatedBy 설정

// 삭제 정보 설정
user.setDeletedInfo(deletedBy);    // deleted=true, deletedAt, deletedBy 설정

// 삭제 여부 확인
boolean isDeleted = user.isDeleted();
```

---

## 💡 사용 패턴

### 1. 인증 토큰 생성 패턴

```java
@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CookieUtil cookieUtil;
    
    @PostMapping("/login")
    public ApiResponse<TokenVO> login(
            @RequestBody LoginVO loginVO, 
            HttpServletResponse response) {
        
        // 1. 인증
        UserVO user = authService.validateLogin(loginVO);
        
        // 2. JWT 토큰 생성
        String jwtToken = jwtTokenUtil.generateNormalLoginToken(user);
        
        // 3. 쿠키 설정
        cookieUtil.setAuthTokenCookie(response, jwtToken);
        
        return ApiResponse.success(new TokenVO(jwtToken));
    }
}
```

### 2. 비밀번호 암호화 패턴

```java
@Service
public class UserService {
    
    public UserVO createUser(UserVO user) {
        try {
            // 1. 비밀번호 암호화
            String encryptedPassword = SecureHashAlgorithm.encryptSHA256(user.getPassword());
            user.setPassword(encryptedPassword);
            
            // 2. 사용자 생성
            userMapper.insert(user);
            
            return user;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("비밀번호 암호화 실패", e);
            throw new BusinessException(Status.시스템오류);
        }
    }
    
    public boolean validatePassword(String rawPassword, String encryptedPassword) {
        try {
            String encryptedInput = SecureHashAlgorithm.encryptSHA256(rawPassword);
            return encryptedInput.equals(encryptedPassword);
            
        } catch (NoSuchAlgorithmException e) {
            log.error("비밀번호 검증 실패", e);
            return false;
        }
    }
}
```

### 3. 권한 기반 접근 제어 패턴

```java
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserVO>> getAllUsers(Authentication auth) {
        UserRole currentRole = extractUserRole(auth);
        
        // 권한별 다른 데이터 제공
        List<UserVO> users;
        
        if (currentRole.isSuperAdmin()) {
            // 모든 사용자 조회
            users = userService.findAll();
        } else if (currentRole.isAdmin()) {
            // 일반 사용자만 조회
            users = userService.findAllExceptAdmins();
        } else {
            throw new BusinessException(Status.권한없음);
        }
        
        return ApiResponse.success(users);
    }
    
    private UserRole extractUserRole(Authentication auth) {
        String authority = auth.getAuthorities().iterator().next().getAuthority();
        return UserRole.fromAuthority(authority);
    }
}
```

---

## ⚠️ 주의사항

### 1. JWT 토큰 보안

```java
// ✅ 올바른 사용
@Value("${jwt.secret}")
private String secret;  // 환경변수에서 주입

// ❌ 하드코딩 (절대 금지)
private String secret = "my-secret-key";
```

### 2. 비밀번호 암호화

```java
// ✅ SHA-256 사용 (레거시 호환)
String encrypted = SecureHashAlgorithm.encryptSHA256(password);

// ✅ BCrypt 사용 권장 (새로운 프로젝트)
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encrypted = encoder.encode(password);
```

### 3. BaseVO 상속

```java
// ✅ 모든 VO는 BaseVO를 상속
public class UserVO extends BaseVO {
    // ...
}

// ❌ BaseVO 없이 자체 구현 (일관성 부족)
public class UserVO {
    private LocalDateTime createdAt;  // 중복 코드
    // ...
}
```

---

## 📚 참고 자료

- **클래스**: `com.zinidata.common.util.*`
- **Enum**: `com.zinidata.common.enums.*`
- **VO**: `com.zinidata.common.vo.BaseVO`
- **API Response**: `/docs/04-common-api-response.md`
- **Exception**: `/docs/05-common-exception-handling.md`

