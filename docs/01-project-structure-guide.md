# 🏗️ ZiniData 프로젝트 구조 가이드

> **ZiniData Spring Boot 엔터프라이즈 프로젝트의 전체 구조를 정리한 문서입니다.**

## 📊 프로젝트 개요

### 🎯 **프로젝트 정보**
- **프로젝트명**: ZiniData
- **기술 스택**: Spring Boot 3.4.7 + Java 17
- **아키텍처**: 멀티 레이어 아키텍처 + 도메인 주도 설계
- **데이터베이스**: PostgreSQL (Primary) + MySQL (SMS)
- **캐시**: Redis (세션 + 캐싱)
- **프론트엔드**: Thymeleaf + TailwindCSS

### 📈 **구현 현황**
- **총 Java 클래스**: 134개 (+1 BrowserOnlyInterceptor 추가)
- **패키지 수**: 62개 (+1 security/interceptor 추가)
- **완성도**: 87% (Phase 2 진행 중 - 보안 강화 완료)

---

## 🌐 웹서비스 구조

### 📱 **메뉴 구조**
```
🏠 홈 (Home)
├── 메인 대시보드
└── 서비스 소개

🔍 탐색 (Explorer) 
├── 📊 종합보고서 (Summary)
├── 👥 유동인구 (Flowpop)  
├── 🏪 점포밀집도 (Density)
├── 📈 상권트렌드 (Markets)
└── ⭐ 프리미엄보고서 (Premium)

📰 뉴스 (News)
├── 뉴스 목록
└── 뉴스 상세

💰 가격 (Pricing)
├── 요금제 안내
└── 결제 관리
```

### 🔗 **URL 매핑 구조**
```
/                    → 홈페이지
/explorer/**         → 탐색 관련 페이지
  ├── /summary       → 종합보고서  
  ├── /flowpop       → 유동인구
  ├── /density       → 점포밀집도
  ├── /markets       → 상권트렌드
  └── /premium       → 프리미엄보고서
/news/**             → 뉴스 관련 페이지
/pricing/**          → 가격 관련 페이지
/auth/**             → 인증 관련 페이지
/mypage/**           → 마이페이지
/api/**              → REST API
/actuator/**         → 운영 모니터링
```

---

## 🏗️ Java 패키지 구조

### 📁 **최상위 구조**
```
src/main/java/com/zinidata/
├── ZiniDataApplication.java    # 메인 애플리케이션
├── audit/                      # 감사로그 시스템
├── common/                     # 공통 컴포넌트
├── config/                     # 설정 관리
├── domain/                     # 비즈니스 도메인
└── security/                   # 보안 시스템
```

### 🔍 **Domain 패키지 상세**
```
domain/
├── common/                     # 공통 도메인 (어떤 프로젝트든 사용 가능)
│   ├── admin/                  # 관리자 기능
│   ├── auth/                   # 인증/인가 시스템  
│   ├── batch/                  # 배치 작업
│   ├── cert/                   # 인증서/SMS 인증
│   ├── docs/                   # 문서 시스템
│   ├── file/                   # 파일 업/다운로드
│   ├── locationsearch/         # 위치 검색 (카카오 API)
│   ├── oauth/                  # 소셜 로그인 (카카오/구글)
│   ├── period/                 # 기간 관리
│   ├── region/                 # 지역 정보 관리
│   ├── sms/                    # SMS 발송 시스템
│   ├── template/               # 템플릿 관리
│   ├── tools/                  # 개발 도구
│   ├── upjong/                 # 업종 정보 관리
│   └── user/                   # 사용자 관리
├── explorer/                   # 탐색 관련 도메인 (프로젝트 전용)
│   ├── api/                    # 탐색 API 컨트롤러
│   ├── controller/             # 탐색 웹 컨트롤러
│   ├── mapper/                 # 탐색 DB 매퍼
│   └── service/                # 탐색 비즈니스 로직
└── main/                       # 메인 페이지 도메인
    └── controller/             # 홈페이지 컨트롤러
```

### 🔒 **Security 패키지 상세**
```
security/
├── config/                     # 보안 설정
│   ├── SecuritySessionConfig.java
│   ├── SessionConfig.java
│   └── WebSecurityConfig.java
├── filter/                     # 보안 필터
│   └── RateLimitFilter.java
├── handler/                    # 보안 핸들러
│   ├── CustomAccessDeniedHandler.java
│   ├── CustomAuthenticationEntryPoint.java
│   ├── CustomAuthenticationFailureHandler.java
│   ├── CustomAuthenticationSuccessHandler.java
│   ├── CustomLogoutHandler.java
│   ├── CustomLogoutSuccessHandler.java
│   └── CustomSessionInformationExpiredStrategy.java
├── interceptor/                # 보안 인터셉터 ✨ 신규 추가
│   └── BrowserOnlyInterceptor.java  # 브라우저 전용 접근 제어
├── properties/                 # 보안 프로퍼티
│   └── SecurityProperties.java
└── ratelimit/                  # Rate Limiting
    ├── exception/
    ├── service/
    └── util/
```

### 📊 **Audit 패키지 상세**
```
audit/
├── annotation/                 # 감사로그 어노테이션
│   └── AuditLog.java
├── aspect/                     # AOP 관련
│   ├── AuditLogAspect.java
│   └── PerformanceAspect.java
├── enums/                      # 감사로그 열거형
│   ├── AuditActionType.java
│   └── AuditResultStatus.java
├── mapper/                     # 감사로그 DB 접근
│   └── AuditLogMapper.java
├── service/                    # 감사로그 서비스
│   └── AuditLogService.java
└── vo/                         # 감사로그 VO
    └── AuditLogVO.java
```

---

## 🎨 프론트엔드 구조

### 📁 **리소스 구조**
```
src/main/resources/
├── static/                     # 정적 리소스
│   └── assets/
│       ├── css/                # CSS 파일
│       │   ├── output.css      # TailwindCSS 빌드 결과
│       │   ├── tailwind/       # TailwindCSS 소스
│       │   └── docs/           # 문서용 CSS
│       ├── js/                 # JavaScript 파일
│       │   ├── auth/           # 인증 관련 JS
│       │   ├── explorer/       # 탐색 관련 JS
│       │   └── zinidata/       # 핵심 비즈니스 JS
│       ├── images/             # 이미지 파일
│       ├── fonts/              # 폰트 파일
│       └── lib/                # 외부 라이브러리
└── templates/                  # Thymeleaf 템플릿
    ├── fragments/              # 공통 Fragment
    │   ├── head.html           # 헤드 설정
    │   ├── header.html         # 헤더 컴포넌트
    │   ├── footer.html         # 푸터 컴포넌트
    │   └── script.html         # 스크립트 로딩
    ├── auth/                   # 인증 관련 페이지
    ├── explorer/               # 탐색 관련 페이지
    ├── mypage/                 # 마이페이지
    └── error/                  # 에러 페이지
```

### 🎨 **CSS 관리 방식**
- **TailwindCSS**: 메인 스타일링 프레임워크
- **컴포넌트 기반**: Fragment별 스타일 분리
- **페이지별 CSS**: 필요 시 페이지별 추가 CSS
- **반응형 디자인**: PC/Mobile 대응

### 📱 **JavaScript 관리 방식**
- **모듈별 분리**: 기능별 JS 파일 분리
- **페이지별 로딩**: `currentPage` 조건부 로딩
- **버전 관리**: 캐시 버스팅 적용
- **jQuery 기반**: jQuery 생태계 활용

---

## ⚙️ 설정 및 환경 관리

### 📄 **설정 파일 구조**
```
src/main/resources/
├── application.yml             # 공통 설정
├── application-develop.yml     # 개발 환경
├── application-staging.yml     # 스테이징 환경
├── application-main.yml        # 운영 환경
├── logback-spring.xml         # 로깅 설정
└── mapper/                    # MyBatis 매퍼 XML
    ├── common/                # 공통 매퍼
    ├── explorer/              # 탐색 매퍼
    └── audit/                 # 감사로그 매퍼
```

### 🔧 **주요 설정 항목**
- **데이터베이스**: PostgreSQL (Primary) + MySQL (SMS)
- **캐시**: Redis (세션 + 데이터 캐싱)  
- **보안**: Spring Security 6.x + Redis 세션
- **브라우저 접근 제어**: 환경별 설정 ✨ 신규 추가
  - develop: `security.browser-check.enabled: false`
  - staging/main: `security.browser-check.enabled: true`
- **암호화**: Jasypt 3.0.5
- **파일**: AWS S3 연동
- **API**: OpenAPI 3.0 문서화

---

## 🗄️ 데이터베이스 구조

### 🏢 **Primary Database (PostgreSQL)**
```
주요 테이블:
├── member              # 회원 정보
├── member_auth         # 회원 인증 정보
├── audit_log           # 감사 로그
├── ip_block            # IP 차단 관리
├── upjong              # 업종 정보
├── region              # 지역 정보
├── explorer_*          # 탐색 관련 테이블
└── template_*          # 템플릿 관련 테이블
```

### 📱 **SMS Database (MySQL)**
```
SMS 전용 테이블:
├── sms_history         # SMS 발송 이력
├── sms_template        # SMS 템플릿
└── cert_history        # 인증 이력
```

### 🔄 **Redis 캐시 구조**
```
Redis 키 구조:
├── session:*           # HTTP 세션 데이터
├── cache:upjong:*      # 업종 정보 캐시
├── cache:region:*      # 지역 정보 캐시
├── ratelimit:*         # Rate Limiting 데이터
└── auth:*              # 인증 관련 임시 데이터
```

---

## 🔒 보안 아키텍처

### 🛡️ **인증/인가 시스템**
```
인증 플로우:
1. 로그인 요청 → AuthController
2. 인증 처리 → Spring Security
3. 세션 생성 → Redis Session Store
4. 권한 검증 → Role-based Access Control
5. 감사 로그 → AuditLogAspect
```

### 🚧 **보안 필터 체인**
```
Filter Chain:
1. SessionExpirationFilter      # 세션 만료 체크
2. RateLimitFilter             # Rate Limiting
3. SessionAuthenticationFilter # 세션 인증
4. Spring Security Filters     # 기본 보안 필터

Interceptor Chain:
1. BrowserOnlyInterceptor      # 브라우저 전용 접근 제어 ✨ 신규 추가
   - /api/explorer/** (보고서 생성 API)
   - /api/business/** (비즈니스 API)
   - /api/member/** (회원 정보 API)
   - /api/mypage/** (마이페이지 API)
```

### 🔐 **암호화 정책**
- **비밀번호**: BCrypt 해싱
- **민감 정보**: Jasypt AES-256 암호화
- **세션**: Redis 암호화 저장
- **API 통신**: HTTPS 강제

---

## 📝 개발 가이드라인

### 🏗️ **아키텍처 패턴**
- **레이어드 아키텍처**: Controller → Service → Mapper
- **도메인 주도 설계**: 비즈니스 도메인별 패키지 분리
- **관심사 분리**: 기능별 명확한 책임 분리

### 🔴 **필수 코딩 규칙**
- **API 요청 파라미터**: 항상 VO 객체로 캡슐화 (`@RequestBody`)
- **Service 클래스**: 인터페이스 없이 직접 구현, 모든 로직은 Service.java 파일 내부에 구현
- **Controller**: Model에 `pageTitle`, `currentPage` 필수 추가
- **ApiController**: 모든 API 메서드에 `HttpServletRequest` 파라미터 추가 및 세션 체크 필수

> 📚 **상세 규칙**: [코딩 규칙 가이드](./07-coding-rules.md) 참고

### 📝 **네이밍 컨벤션**
- **패키지**: 소문자, 단수형 (`auth`, `user`)
- **클래스**: PascalCase + 접미사 (`AuthController`, `UserService`)
- **메서드**: camelCase + 동사 (`getUserById`, `createMember`)
- **URL**: kebab-case (`/auth/login`, `/api/auth/check-password`)

### 🔄 **확장 가이드**
```
새로운 도메인 추가 시:
1. domain/{새도메인}/ 패키지 생성
2. controller/service/mapper/vo 구조 생성  
3. 해당 매퍼 XML 파일 추가
4. 테스트 코드 작성
5. API 문서 업데이트
```

---

## 📊 향후 확장 계획

### 🚀 **Phase 3 계획**
- [ ] **뉴스 도메인** 구현 (`domain/news/`)
- [ ] **가격 도메인** 구현 (`domain/pricing/`)
- [ ] **마이페이지** 고도화
- [ ] **모바일 최적화** 강화
- [ ] **성능 최적화** 및 모니터링

### 🌐 **마이크로서비스 대비**
현재 모놀리식 구조이지만, 도메인별 패키지 분리로 향후 마이크로서비스 분할 가능:
```
분할 가능 서비스:
├── auth-service        # 인증 서비스
├── explorer-service    # 탐색 서비스  
├── news-service        # 뉴스 서비스
├── pricing-service     # 가격 서비스
└── common-service      # 공통 서비스
```

---

## 📚 관련 문서

### 🏗️ **아키텍처 & 개발 가이드**
- [코드 작성 가이드](./코드%20작성%20가이드.md)
- [프로젝트 개발 가이드](./프로젝트%20개발%20가이드.md)

### 🔒 **보안 가이드**
- [API 접근 제어 가이드](../security/01-api-access-control-guide.md) ✨ 신규 추가

### 🔑 **기능별 가이드**
- [로그인 시스템 가이드](../features/01-login-system-guide.md)

---

**📅 작성일**: 2025년 9월 1일  
**📍 버전**: v1.0  
**✅ 상태**: 현재 구조 기준 정리 완료
