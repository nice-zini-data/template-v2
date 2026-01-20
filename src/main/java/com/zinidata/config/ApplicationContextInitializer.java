package com.zinidata.config;

import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * 애플리케이션 전역 정보 설정
 * JSP에서 ${applicationScope.xxx} 형태로 사용할 수 있는 정보들을 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationContextInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;
    
    @Value("${spring.application.name:nicebizmap}")
    private String applicationName;
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.code:NBZM}")
    private String appCode;
    
    @Value("${spring.application.name:nicebizmap}")
    private String appName;
    
    @Value("${custom.debug.enabled:false}")
    private boolean debugEnabled;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            WebApplicationContext webContext = (WebApplicationContext) event.getApplicationContext();
            ServletContext servletContext = webContext.getServletContext();
            
            if (servletContext != null) {
                // 애플리케이션 기본 정보
                servletContext.setAttribute("appCode", appCode);
                servletContext.setAttribute("appName", appName);
                servletContext.setAttribute("appVersion", appVersion);
                servletContext.setAttribute("applicationName", applicationName);
                
                // 환경 정보
                String activeProfile = String.join(",", environment.getActiveProfiles());
                if (activeProfile.isEmpty()) {
                    activeProfile = "default";
                }
                servletContext.setAttribute("activeProfile", activeProfile);
                
                // 서버 정보
                String serverPort = environment.getProperty("server.port", "8080");
                servletContext.setAttribute("serverPort", serverPort);
                
                // 빌드 정보 (선택사항)
                String buildTime = environment.getProperty("app.build.time", "Unknown");
                servletContext.setAttribute("buildTime", buildTime);
                
                // 환경별 기능 플래그
                boolean isProduction = activeProfile.contains("prod");
                boolean isDevelopment = activeProfile.contains("local") || activeProfile.contains("dev");
                servletContext.setAttribute("isProduction", isProduction);
                servletContext.setAttribute("isDevelopment", isDevelopment);
                servletContext.setAttribute("debugEnabled", debugEnabled);
                
                log.info("====================================");
                log.info("Application Context Initialized");
                log.info("====================================");
                log.info("App Code: {}", appCode);
                log.info("App Name: {}", appName);
                log.info("App Version: {}", appVersion);
                log.info("Active Profile: {}", activeProfile);
                log.info("Server Port: {}", serverPort);
                log.info("Is Production: {}", isProduction);
                log.info("Is Development: {}", isDevelopment);
                log.info("DEBUG ENABLED: {}", debugEnabled);
                log.info("====================================");
            }
        } catch (Exception e) {
            log.error("Failed to initialize application context", e);
        }
    }
} 