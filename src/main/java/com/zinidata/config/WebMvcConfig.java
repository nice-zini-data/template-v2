package com.zinidata.config;

import com.zinidata.security.interceptor.BrowserOnlyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final BrowserOnlyInterceptor browserOnlyInterceptor;

    // CORS 설정은 WebSecurityConfig에서 통합 관리

    /**
     * 정적 리소스 설정 (Spring Boot 표준 방식)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Spring Boot 표준: classpath:/static/assets -> /assets/** 매핑
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(3600)
                .resourceChain(true);
                
        // 파일 업로드 디렉토리 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
        
        // Spring Boot 기본 정적 리소스는 자동 처리됨
        // favicon.ico는 /static/favicon.ico에서 자동으로 처리됨
    }
    
    /**
     * 인터셉터 설정 - 브라우저 전용 접근 제어
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(browserOnlyInterceptor)
                .addPathPatterns(
                    "/api/explorer/**",      // 탐색 관련 API (보고서 생성)
                    "/api/business/**",      // 비즈니스 API
                    "/api/member/**",        // 회원 정보 API
                    "/api/mypage/**",        // 마이페이지 API
                    "/api/common/**"         // 공통 API (테스트용)
                )
                .excludePathPatterns(
                    "/api/auth/login",        // 로그인 API 제외
                    "/api/auth/logout",       // 로그아웃 API 제외
                    "/api/auth/signup",       // 회원가입 API 제외
                    "/api/auth/check-*",      // 중복 체크 API 제외
                    "/actuator/**",           // 헬스체크 API 제외
                    "/api/public/**",         // 공개 API 제외
                    "/api/common/region/**",  // 지역 정보 공개 API 제외
                    "/api/cert/**"            // 로그인전 휴대폰 번호 인증을 위해 제외
                );
    }
} 