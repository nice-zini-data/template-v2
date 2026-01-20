# 🎨 TailwindCSS 가이드

> **ZiniData 프로젝트 TailwindCSS 사용 가이드**

## 📌 개요

이 프로젝트는 **TailwindCSS 3.4.17**을 사용하여 유틸리티 우선 CSS 프레임워크를 구현하고 있습니다. 커스텀 색상, 간격, 텍스트 크기 등이 프로젝트 요구사항에 맞게 확장되어 있습니다.

### 🎯 주요 특징
- **유틸리티 우선**: 클래스를 직접 작성해 CSS 파일 없이 빠르게 스타일링
- **커스텀 디자인 시스템**: 프로젝트 전용 색상, 간격, 텍스트 스케일
- **반응형**: 모바일, 태블릿, 데스크톱 대응
- **PurgeCSS**: 프로덕션 빌드 시 사용되지 않는 CSS 자동 제거

---

## ⚙️ 설정 파일

### 📁 파일 구조

```
프로젝트 루트/
├── tailwind.config.js          # TailwindCSS 설정
├── postcss.config.js           # PostCSS 설정
├── package.json                # npm 스크립트 정의
└── src/main/resources/static/assets/css/
    └── tailwind/
        ├── style.css          # TailwindCSS 진입점
        └── common.css          # 커스텀 컴포넌트 스타일
```

### 📝 tailwind.config.js

주요 설정:

- **content**: 스캔할 파일 경로 정의
  ```javascript
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/**/*.js',
    './src/main/webapp/**/*.{jsp,html,js}'
  ]
  ```

- **커스텀 색상 팔레트**: `primary`, `zinc`, `gray` 등 상세한 색상 스케일
- **커스텀 간격**: `0-5`, `1-5`, `2-5` 등 소수점 단위
- **커스텀 텍스트 크기**: `md` (0.9375rem), `2xs` (0.75rem) 등
- **커스텀 breakpoint**: 모바일부터 데스크톱까지 세밀한 반응형 제어

### 📝 postcss.config.js

PostCSS는 TailwindCSS와 Autoprefixer를 연결합니다:

```javascript
module.exports = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

---

## 🔨 빌드 명령어

### 개발 모드 (Watch)

```bash
npm run watch
```

파일 변경 시 자동으로 CSS를 재빌드합니다. 개발 중에는 이 명령어를 실행해두세요.

### 프로덕션 빌드

```bash
npm run build-prod
```

CSS를 최소화(minify)하여 최적화된 파일을 생성합니다.

### 일반 빌드

```bash
npm run build
```

개발 빌드와 동일하지만 최소화하지 않습니다.

---

## 🎨 커스텀 디자인 시스템

### 색상 팔레트

#### Primary (프로젝트 메인 컬러)

```html
<!-- 색상 예시 -->
<div class="bg-primary-500">Primary 500 (#2b7fff)</div>
<div class="bg-primary-600">Primary 600 (#155dfc)</div>
<div class="text-primary-600">Primary 텍스트</div>
```

50단계 계조:
- `primary-50` (가장 밝음)
- `primary-100`
- ...
- `primary-950` (가장 어두움)

#### Zinc (중성색)

```html
<div class="bg-zinc-50">밝은 회색</div>
<div class="bg-zinc-500">중간 회색</div>
<div class="bg-zinc-900">어두운 회색</div>
```

#### 추가 색상

`red`, `orange`, `amber`, `yellow`, `green`, `blue`, `purple`, `pink` 등이 모두 50-950 스케일로 제공됩니다.

### 간격 (Spacing)

```html
<!-- 예시 -->
<div class="p-1-5">패딩 6px</div>
<div class="m-2-5">마진 10px</div>
<div class="px-3-5">좌우 패딩 14px</div>
```

주요 간격:
- `0-5`: 2px
- `1`: 4px
- `1-5`: 6px
- `2-5`: 10px
- `3-5`: 14px

### 텍스트 크기

```html
<p class="text-2xs">0.75rem</p>
<p class="text-xs">0.8125rem</p>
<p class="text-md">0.9375rem</p>
<p class="text-base">1.0rem</p>
<p class="text-lg">1.125rem</p>
```

### 라인 높이

```html
<p class="text-sm/5">텍스트 크기 0.875rem, 라인 높이 1.25rem</p>
<p class="text-base/6">텍스트 크기 1.0rem, 라인 높이 1.5rem</p>
```

형식: `text-{size}/{lineHeight}`

---

## 📱 반응형 Breakpoint

### 기본 Breakpoint

```css
/* 모바일: 기본 (360px 이하) */
<div class="text-base">기본</div>

/* 태블릿: 641px 이상 */
<div class="sm:text-lg">작은 화면</div>

/* 중간: 768px 이상 */
<div class="md:text-xl">중간 화면</div>

/* 큰 화면: 1024px 이상 */
<div class="lg:text-2xl">큰 화면</div>

/* 매우 큰 화면: 1280px 이상 */
<div class="xl:text-3xl">매우 큰 화면</div>

/* 초대형: 1366px 이상 */
<div class="2xl:text-4xl">초대형 화면</div>
```

### Max-width Breakpoint

특정 크기 **이하**에서 적용:

```html
<!-- 768px 이하 -->
<div class="max-md:hidden">큰 화면에서만 보임</div>

<!-- 1024px 이하 -->
<div class="max-lg:text-sm">큰 화면에서는 작은 텍스트</div>
```

주요 breakpoint:
- `max-xs`: 360px 이하
- `max-sm`: 640px 이하
- `max-md`: 768px 이하
- `max-lg`: 1024px 이하
- `max-xl`: 1280px 이하
- `max-2xl`: 1366px 이하

---

## 💡 사용 예시

### 버튼 스타일링

```html
<!-- Primary 버튼 -->
<button class="bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-500">
  Primary 버튼
</button>

<!-- Outline 버튼 -->
<button class="border-2 border-primary-600 text-primary-600 px-4 py-2 rounded-md hover:bg-primary-50">
  Outline 버튼
</button>

<!-- 비활성화 상태 -->
<button class="bg-zinc-300 text-white px-4 py-2 rounded-md cursor-not-allowed">
  비활성화
</button>
```

### 카드 레이아웃

```html
<div class="bg-white rounded-2xl shadow-md p-6 max-sm:p-4">
  <h2 class="text-xl font-bold mb-4">카드 제목</h2>
  <p class="text-sm/5 text-zinc-600">카드 내용</p>
</div>
```

### 반응형 그리드

```html
<div class="grid grid-cols-1 max-sm:grid-cols-1 max-lg:grid-cols-2 lg:grid-cols-3 gap-4">
  <div class="bg-gray-100 p-4">아이템 1</div>
  <div class="bg-gray-100 p-4">아이템 2</div>
  <div class="bg-gray-100 p-4">아이템 3</div>
</div>
```

### Flexbox 레이아웃

```html
<div class="flex justify-between items-center max-lg:flex-col max-lg:gap-4">
  <h1 class="text-2xl font-bold">제목</h1>
  <button class="px-4 py-2 bg-primary-600 text-white">액션</button>
</div>
```

---

## 🎯 커스텀 컴포넌트 (common.css)

`common.css`에 정의된 재사용 컴포넌트들:

### 버튼 컴포넌트

```html
<button class="primaryBtn">Primary 버튼</button>
<button class="softBtn">Soft 버튼</button>
<button class="grayLineBtn">Gray Line 버튼</button>
```

### 입력 필드

```html
<label class="inputLabel">이메일</label>
<input type="email" class="inputCustom" placeholder="이메일을 입력하세요">
```

### 박스 레이아웃

```html
<div class="whiteBox">
  <h2 class="titleText">제목</h2>
  <p class="titleSubText">부제목</p>
</div>

<div class="grayLineBox">
  <!-- 콘텐츠 -->
</div>
```

### 센터 레이아웃 유틸리티

```html
<div class="flexCenter">중앙 정렬</div>
<div class="flexBetween">양쪽 정렬</div>
<div class="flexAround">균등 분배</div>
```

---

## 🚀 개발 워크플로우

### 1. 개발 시작

```bash
# Terminal 1: 백엔드 서버
mvn spring-boot:run

# Terminal 2: TailwindCSS Watch
npm run watch
```

### 2. 스타일 작성

Thymeleaf 템플릿에서 클래스를 추가:

```html
<div class="bg-primary-500 text-white p-4 rounded-lg">
  새로운 스타일
</div>
```

### 3. 자동 빌드

Watch 모드가 파일 변경을 감지하면 자동으로 `output.css`를 재생성합니다.

### 4. 브라우저 새로고침

변경사항을 확인합니다.

---

## 📊 파일 참조

### HTML에서 CSS 사용

```html
<link rel="stylesheet" href="/assets/css/output.css">
```

### 개발 중 (Thymeleaf)

```html
<link rel="stylesheet" th:href="@{/assets/css/output.css}">
```

---

## ⚠️ 주의사항

### 1. Purging

TailwindCSS는 프로덕션 빌드 시 사용되지 않는 CSS를 제거합니다. 동적으로 생성되는 클래스는 `content` 설정에 포함되지 않으면 사라질 수 있습니다.

### 2. Whitespace

HTML에서 줄바꿈 제거 시 클래스가 합쳐지지 않도록 주의:

```html
<!-- ❌ 잘못된 예시 -->
<div class="flex justify-center items-center
           bg-white rounded-lg shadow-md">

<!-- ✅ 올바른 예시 -->
<div class="flex justify-center items-center bg-white rounded-lg shadow-md">
```

### 3. 커스텀 클래스

TailwindCSS가 감지하지 못하는 커스텀 클래스를 많이 사용할 경우, `output.css`에 포함하지 않으면 스타일이 적용되지 않습니다.

### 4. 색상 변수

일부 `--css-variable`가 정의되어 있지만, TailwindCSS는 HTML 클래스를 우선 사용합니다.

---

## 🔍 유용한 팁

### 1. IntelliSense

VS Code의 Tailwind CSS IntelliSense 확장을 설치하면 자동완성을 제공합니다.

### 2. 브라우저 DevTools

개발자 도구의 Elements 탭에서 사용 중인 클래스를 확인할 수 있습니다.

### 3. Just-in-time 모드

TailwindCSS는 JIT(Just-In-Time) 모드를 기본 사용하므로 필요한 클래스만 생성합니다.

### 4. 임의 값 사용

TailwindCSS가 지원하지 않는 임의 값도 사용 가능:

```html
<div class="bg-[#ff6b6b] text-[14px]">임의 값</div>
```

---

## 📚 참고 자료

- **공식 문서**: https://tailwindcss.com/docs
- **Playground**: https://play.tailwindcss.com
- **설정 파일**: `/tailwind.config.js`
- **커스텀 스타일**: `/src/main/resources/static/assets/css/tailwind/common.css`

---

## 🎓 체크리스트

새로운 컴포넌트를 만들 때:

- [ ] TailwindCSS 클래스 사용
- [ ] 반응형 breakpoint 추가 (`max-sm:`, `max-lg:` 등)
- [ ] 커스텀 색상이 필요하면 `primary-*`, `zinc-*` 등 사용
- [ ] 공통 스타일은 `common.css`에 추가 고려
- [ ] `npm run build-prod` 실행하여 최종 빌드 확인

