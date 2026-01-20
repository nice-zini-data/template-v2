# zinidata
nicevanas-enterprise

## 프로젝트 구조

### 핵심 폴더
- `src/` - 소스코드
- `docs/` - 현재 문서
- `docker/` - 도커 설정
- `aws/` - AWS 설정
- `sql/` - 데이터베이스 스크립트
- `sms/` - SMS 관련
- `assets/` - 정적 자산
- `files/` - 파일 저장소

### 설정 파일
- `pom.xml` - Maven 설정
- `package.json` - npm 설정
- `tailwind.config.js` - Tailwind CSS 설정
- `postcss.config.js` - PostCSS 설정
- `mvnw`, `mvnw.cmd` - Maven Wrapper

### 불필요한 파일 (삭제 가능)
- `tailwindcss 가이드.md` - 개발 가이드 문서
- `logs/` - 로그 파일들
  - `nicevanas.log` - 메인 로그 (모든 `log.info()`, `log.debug()` 등)
  - `nicevanas-error.log` - 에러 로그 (`log.error()` 호출만 자동 분리)
  - `nicevanas-audit.log` - 감사 로그 (`@AuditLog` 어노테이션 메서드)
  - `nicevanas-performance.log` - 성능 로그 (`@PerformanceLog` 어노테이션 메서드)
- `target/` - Maven 빌드 결과물
- `node_modules/` - npm 의존성 (재생성: `npm install`)
    ```bash
    npm install      # 의존성 설치
    npm run build    # CSS 빌드
    npm run watch    # CSS 실시간 빌드
    ```