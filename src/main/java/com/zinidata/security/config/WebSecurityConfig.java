package com.zinidata.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;

import com.zinidata.domain.common.auth.filter.SessionAuthenticationFilter;
import com.zinidata.domain.common.auth.filter.SessionExpirationFilter;
import com.zinidata.security.filter.RateLimitFilter;
import com.zinidata.security.handler.CustomAccessDeniedHandler;
import com.zinidata.security.handler.CustomAuthenticationEntryPoint;
import com.zinidata.security.handler.CustomAuthenticationSuccessHandler;
import com.zinidata.security.handler.CustomLogoutHandler;
import com.zinidata.security.handler.CustomLogoutSuccessHandler;
import com.zinidata.security.handler.CustomSessionInformationExpiredStrategy;
import com.zinidata.security.properties.SecurityProperties;
import com.zinidata.security.provider.CustomAuthenticationProvider;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 웹 보안 설정
 * Redis 세션 기반 인증 및 권한 관리
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final SecurityProperties securityProperties;
    private final RateLimitFilter rateLimitFilter;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;
    private final CustomLogoutHandler logoutHandler;
    private final CustomSessionInformationExpiredStrategy sessionExpiredStrategy;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final SessionExpirationFilter sessionExpirationFilter;
    private final SessionRegistry sessionRegistry;
    private final CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final CustomAuthenticationProvider customAuthenticationProvider;

    /**
     * 커스텀 AuthenticationProvider 설정
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    /**
     * 보안 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 설정 (REST API 로그인을 위해 비활성화)
            .csrf(csrf -> csrf.disable())
            
            // 헤더 설정
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::and)
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true)
                )
            )
            
            // 세션 관리 - 중복 로그인 차단 설정
            .sessionManagement(session -> {
                int maxSessions = securityProperties.getMaxSessions();
                boolean preventLogin = securityProperties.isPreventLoginIfMaximumExceeded();
                
                // 🔍 디버그: 실제 설정값 확인
                System.out.println("🔧 [SECURITY-CONFIG] maxSessions: " + maxSessions);
                System.out.println("🔧 [SECURITY-CONFIG] preventLoginIfMaximumExceeded: " + preventLogin);
                
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)       // ✅ 세션 생성 정책
                    .maximumSessions(maxSessions)                                   // ✅ 최대 세션 수 제한
                    .maxSessionsPreventsLogin(false)                                // ✅ 새 로그인 허용, 기존 세션 만료
                    .sessionRegistry(sessionRegistry)                               // ✅ 세션 레지스트리
                    .expiredSessionStrategy(sessionExpiredStrategy)                 // ✅ 세션 만료 처리 전략
                    .and()
                    .sessionFixation().migrateSession()                             // ✅ 세션 고정 공격 방지 (changeSessionId 대신 migrateSession 사용)
                    .sessionAuthenticationStrategy(sessionAuthenticationStrategy);  // ✅ 세션 인증 전략
            })
            
            // 권한 설정
            .authorizeHttpRequests(auth -> auth
                // ==================== 가장 중요한 API 먼저 설정 ====================
                
                // ==================== GUEST (비회원) 접근 가능 ====================
                // 정적 리소스
                .requestMatchers("/static/**", "/assets/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/WEB-INF/views/**").permitAll() // JSP 뷰 리졸버 경로 (임시 유지)
                
                // 홈 페이지 관련 페이지
                .requestMatchers("/").permitAll()                      // 홈 페이지
                .requestMatchers("/home").permitAll()                  // 홈 페이지
                
                // 인증 관련 페이지
                .requestMatchers("/auth/**").permitAll()               // 인증 페이지

                // API 접근 권한 설정
                .requestMatchers("/api/auth/**").permitAll()           // 인증 API
                .requestMatchers("/api/cert/**").permitAll()           // 인증서 API (로그인 전 휴대폰 인증)
                
                // ==================== 나머지는 인증 필요 ====================
                .anyRequest().authenticated()
            )
            
            // 로그인 설정 (REST API 방식 사용)
            .formLogin(form -> form
                .loginPage("/auth/login")
                .permitAll()
            )
            
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")           // ✅ 로그아웃 URL 설정
                .addLogoutHandler(logoutHandler)         // ✅ 커스텀 로그아웃 핸들러 (감사 로그용)
                .logoutSuccessHandler(logoutSuccessHandler)        // ✅ 커스텀 성공 핸들러
                .invalidateHttpSession(true) // ✅ 세션 무효화
                .clearAuthentication(true)     // ✅ 인증 정보 삭제
                .deleteCookies("JSESSIONID") // ✅ 쿠키 삭제
                .permitAll()                                       // ✅ 모든 사용자 허용
            )
            
            // 예외 처리
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            
            // Rate Limiting 필터 추가
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 세션 만료 감지 필터 추가 (Rate Limit 필터 다음에 추가)
            .addFilterAfter(sessionExpirationFilter, RateLimitFilter.class)
            
            // 세션 인증 필터 추가 (세션 만료 필터 다음에 추가)
            .addFilterAfter(sessionAuthenticationFilter, SessionExpirationFilter.class);

        return http.build();
    }

    /**
     * 패스워드 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS 설정 (SecurityProperties 기반)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // SecurityProperties에서 설정값 가져오기
        SecurityProperties.Cors corsProps = securityProperties.getCors();
        
        // 허용할 오리진 설정 (와일드카드 패턴 지원)
        String[] origins = corsProps.getAllowedOrigins().split(",");
        for (String origin : origins) {
            String trimmedOrigin = origin.trim();
            if (trimmedOrigin.contains("*")) {
                // 와일드카드 패턴인 경우 addAllowedOriginPattern 사용
                configuration.addAllowedOriginPattern(trimmedOrigin);
            } else {
                // 일반 도메인인 경우 addAllowedOrigin 사용
                configuration.addAllowedOrigin(trimmedOrigin);
            }
        }
        
        // 허용할 HTTP 메서드
        String[] methods = corsProps.getAllowedMethods().split(",");
        for (String method : methods) {
            configuration.addAllowedMethod(method.trim());
        }
        
        // 허용할 헤더
        configuration.addAllowedHeader(corsProps.getAllowedHeaders());
        
        // 인증 정보 허용
        configuration.setAllowCredentials(corsProps.isAllowCredentials());
        
        // 프리플라이트 요청 캐시 시간
        configuration.setMaxAge((long) corsProps.getMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

} 