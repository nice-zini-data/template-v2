# 📋 코딩 규칙 가이드

> **ZiniData 프로젝트 코딩 규칙 및 베스트 프랙티스**

## 📌 개요

이 문서는 프로젝트 전체에서 일관된 코드 품질을 유지하기 위한 코딩 규칙을 정의합니다. 모든 개발자는 이 규칙을 준수해야 합니다.

---

## 🔴 필수 규칙

### 1. API 요청 파라미터 처리 규칙

**❌ 잘못된 예시:**
```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(
    @RequestParam String loginId,
    @RequestParam String password,
    @RequestParam String email) {
    // ...
}
```

**✅ 올바른 예시:**
```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO userVO) {
    // ...
}
```

**규칙:**
- **항상 VO 객체로 캡슐화**: API 요청 파라미터는 개별 파라미터(`@RequestParam`)로 전달하지 말고, 반드시 VO 객체(`@RequestBody`)로 캡슐화하여 전달해야 합니다.
- **이유**: 
  - 코드 가독성 향상
  - 유지보수성 향상
  - 파라미터 검증 및 문서화 용이
  - 확장성 (추가 파라미터 시 VO만 수정)

---

### 2. Service 클래스 구현 규칙

**❌ 잘못된 예시:**
```java
// 인터페이스 정의
public interface UserService {
    UserVO findById(Long userId);
}

// 구현 클래스
@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserVO findById(Long userId) {
        // ...
    }
}
```

**✅ 올바른 예시:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserMapper userMapper;
    
    public UserVO findById(Long userId) {
        // 모든 로직은 여기에 구현
        // ...
    }
}
```

**규칙:**
- **인터페이스 없이 직접 구현**: Service 클래스는 인터페이스를 정의하거나 구현하지 않고, 직접 구현해야 합니다.
- **모든 로직은 Service.java 파일 내부**: 모든 서비스 로직은 Service.java 파일 내부에 구현해야 합니다.
- **이유**:
  - 불필요한 추상화 제거
  - 코드 간소화
  - 유지보수 용이
  - 프로젝트 규모에 맞는 적절한 설계

---

### 3. Controller 구현 규칙

**모든 Controller 메서드는 Model에 필수 속성을 추가해야 합니다.**

**❌ 잘못된 예시:**
```java
@GetMapping("/login")
public String loginPage(Model model) {
    return "auth/login";  // Model 속성 없음
}
```

**✅ 올바른 예시:**
```java
@GetMapping("/login")
public String loginPage(Model model) {
    model.addAttribute("pageTitle", "로그인");
    model.addAttribute("currentPage", "auth");
    return "auth/login";
}
```

**규칙:**
- **pageTitle**: 페이지 제목 (예: "로그인", "요청 등록", "홈")
- **currentPage**: 현재 페이지 구분 (예: "auth", "requests", "home")
- **이유**:
  - 템플릿에서 공통 헤더/푸터에 사용
  - 페이지별 스타일링 및 스크립트 로딩 구분
  - SEO 및 접근성 향상

---

### 4. ApiController 세션 체크 규칙

**모든 ApiController 메서드는 세션 체크를 수행해야 합니다.**

**❌ 잘못된 예시:**
```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO userVO) {
    // 세션 체크 없음
    UserVO createdUser = userService.create(userVO);
    return ApiResponse.success(createdUser);
}
```

**✅ 올바른 예시:**
```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(
        @RequestBody UserVO userVO,
        HttpServletRequest request) {
    
    // 세션 체크
    HttpSession session = request.getSession(false);
    if (session == null) {
        log.warn("[USER_API] 세션이 존재하지 않습니다.");
        ApiResponse<UserVO> response = ApiResponse.unauthorized("로그인이 필요합니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    Long memNo = (Long) session.getAttribute("memNo");
    if (memNo == null) {
        log.warn("[USER_API] 세션에 memNo가 없습니다.");
        ApiResponse<UserVO> response = ApiResponse.unauthorized("유효하지 않은 세션입니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    log.info("[USER_API] 세션에서 memNo 조회 완료 - memNo: {}", memNo);
    
    // 비즈니스 로직 수행
    UserVO createdUser = userService.create(userVO);
    return ApiResponse.success(createdUser);
}
```

**규칙:**
- **HttpServletRequest 파라미터 추가**: 모든 API 메서드에 `HttpServletRequest request` 파라미터를 추가
- **세션 체크 순서**:
  1. `request.getSession(false)`로 세션 조회
  2. 세션이 null이면 UNAUTHORIZED 응답
  3. `session.getAttribute("memNo")`로 회원 번호 확인
  4. memNo가 null이면 UNAUTHORIZED 응답
- **이유**:
  - 보안 강화
  - 인증된 사용자만 API 접근 가능
  - 일관된 인증 처리

---

### 5. Mapper 호출 규칙

**Service에서 Mapper를 호출할 때는 VO 객체를 직접 전달해야 합니다.**

**❌ 잘못된 예시:**
```java
@Service
public class MapService {
    public List<MapVO> getRequestMap(MapVO mapVO) {
        // 개별 변수를 펼쳐서 전달
        List<MapVO> result = mapMapper.requestMap(
            mapVO.getGubun(), 
            mapVO.getMinx(), 
            mapVO.getMiny(), 
            mapVO.getMaxx(), 
            mapVO.getMaxy()
        );
        return result;
    }
}
```

**✅ 올바른 예시:**
```java
@Service
public class MapService {
    public List<MapVO> getRequestMap(MapVO mapVO) {
        // VO 객체를 직접 전달
        List<MapVO> result = mapMapper.requestMap(mapVO);
        return result;
    }
}
```

**Mapper 인터페이스:**
```java
@Mapper
public interface MapMapper {
    // ✅ VO 객체를 파라미터로 받음
    List<MapVO> requestMap(MapVO mapVO);
    
    // ❌ 개별 파라미터로 받지 않음
    // List<MapVO> requestMap(@Param("gubun") String gubun, ...);
}
```

**규칙:**
- **VO 객체 직접 전달**: Service에서 Mapper를 호출할 때 개별 변수를 펼쳐서 전달하지 말고, VO 객체를 직접 전달해야 합니다.
- **Mapper 인터페이스**: Mapper 인터페이스도 VO 객체를 파라미터로 받도록 정의해야 합니다.
- **MyBatis 자동 매핑**: MyBatis는 VO 객체를 파라미터로 받으면 XML에서 `#{gubun}`, `#{minx}` 등으로 자동으로 속성에 접근합니다.
- **이유**:
  - 코드 간소화
  - 유지보수성 향상 (파라미터 추가/제거 시 VO만 수정)
  - 일관성 유지 (다른 Mapper와 동일한 패턴)

---

## 🟡 추가 규칙

### 6. API 응답 구조

**모든 REST API는 `ApiResponse<T>`로 응답해야 합니다.**

```java
@PostMapping("/users")
public ApiResponse<UserVO> createUser(@RequestBody UserVO userVO) {
    UserVO createdUser = userService.create(userVO);
    return ApiResponse.success(createdUser);
}
```

---

### 7. 예외 처리

**Service 계층에서 예외를 발생시키고, Controller에서는 예외를 throw만 합니다.**

```java
// Service
@Service
public class UserService {
    public UserVO findById(Long userId) {
        UserVO user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(Status.데이터없음);
        }
        return user;
    }
}

// Controller (예외 처리 불필요 - GlobalExceptionHandler가 자동 처리)
@GetMapping("/users/{id}")
public ApiResponse<UserVO> getUser(@PathVariable Long id) {
    UserVO user = userService.findById(id);
    return ApiResponse.success(user);
}
```

---

### 8. VO 클래스 작성

**모든 VO는 `BaseVO`를 상속해야 합니다.**

```java
@Getter
@Setter
@NoArgsConstructor
public class UserVO extends BaseVO {
    private Long userId;
    private String loginId;
    // ...
}
```

---

### 9. SQL 작성 규칙

```json
{
  "rules": {
    "ddlCase": "upper",          // DDL 키워드는 대문자
    "keywordCase": "upper",      // SELECT, INSERT 같은 키워드도 대문자
    "identifierCase": "lower",   // 테이블명, 컬럼명, 별명은 소문자
    "functionCase": "lower"      // COUNT, SUM 같은 함수는 소문자
  }
}
```

**예시:**
```sql
SELECT 
    seq,
    crt_id as crtId,
    crt_name as crtName,
    count(*) over() as total_count
FROM tbnvps_service_request
WHERE 1=1
    AND status = '1'
ORDER BY crt_dt DESC;
```

---

### 10. JavaScript/TypeScript 규칙

- **화살표 함수 우선 사용**
- **함수 변수는 `const` 사용**
- **`function` 키워드는 필요한 경우에만 사용**

```javascript
// ✅ 올바른 예시
const getUserById = (id) => {
    return fetch(`/api/users/${id}`);
};

// ❌ 잘못된 예시
function getUserById(id) {
    return fetch(`/api/users/${id}`);
}
```

---

## 📝 네이밍 컨벤션

### Java
- **패키지**: 소문자, 단수형 (`auth`, `user`)
- **클래스**: PascalCase + 접미사 (`AuthController`, `UserService`, `UserVO`)
- **메서드**: camelCase + 동사 (`getUserById`, `createMember`)
- **URL**: kebab-case (`/api/auth/login`, `/api/users/check-id`)

### SQL
- **키워드**: 대문자 (`SELECT`, `INSERT`, `UPDATE`, `DELETE`)
- **테이블명/컬럼명**: 소문자 (`tbnvps_service_request`, `crt_id`)
- **함수**: 소문자 (`count`, `sum`, `max`)

---

## 🎯 체크리스트

새로운 파일을 만들 때 다음 사항을 확인하세요:

### Controller.java
- [ ] Model에 `pageTitle` 속성을 추가했는가?
- [ ] Model에 `currentPage` 속성을 추가했는가?

### ApiController.java
- [ ] 메서드에 `HttpServletRequest request` 파라미터를 추가했는가?
- [ ] 세션 체크 로직을 구현했는가?
- [ ] 세션이 없을 때 UNAUTHORIZED 응답을 반환하는가?
- [ ] memNo가 없을 때 UNAUTHORIZED 응답을 반환하는가?

### Service.java
- [ ] Mapper 호출 시 VO 객체를 직접 전달하는가? (개별 변수 펼치기 금지)

### 공통
- [ ] API 컨트롤러는 `@RequestBody`로 VO 객체를 받는가?
- [ ] Service 클래스는 인터페이스 없이 직접 구현했는가?
- [ ] 모든 서비스 로직이 Service.java 파일 내부에 있는가?
- [ ] API 응답은 `ApiResponse<T>`를 사용하는가?
- [ ] 예외는 Service 계층에서 발생시키는가?
- [ ] VO 클래스는 `BaseVO`를 상속하는가?
- [ ] SQL 키워드는 대문자로 작성했는가?

---

## 📚 관련 문서

- [프로젝트 구조 가이드](./01-project-structure-guide.md)
- [API Response 가이드](./04-common-api-response.md)
- [Exception Handling 가이드](./05-common-exception-handling.md)
- [Common Utilities 가이드](./06-common-utilities.md)

---

**📅 작성일**: 2025년 1월 31일  
**📍 버전**: v1.0  
**✅ 상태**: 필수 규칙 정의 완료

