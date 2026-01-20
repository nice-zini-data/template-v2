package com.zinidata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 컨트롤러 어드바이스
 * 모든 컨트롤러에서 공통으로 사용할 수 있는 모델 속성들을 추가합니다.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private Environment environment;

    /**
     * 환경 정보를 모든 모델에 추가
     */
    @ModelAttribute("env")
    public Map<String, Object> addEnvironmentInfo() {
        Map<String, Object> envInfo = new HashMap<>();
        
        // 활성 프로필
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : "local";
        envInfo.put("activeProfile", activeProfile);
        
        // 환경 플래그
        envInfo.put("isProduction", "main".equals(activeProfile));
        envInfo.put("isDevelopment", !"main".equals(activeProfile));
        
        // 애플리케이션 정보
        envInfo.put("appName", environment.getProperty("app.name", "nicebizmap-service"));
        envInfo.put("appVersion", environment.getProperty("app.version", "1.0.0"));
        envInfo.put("appBaseUrl", environment.getProperty("app.baseUrl", "https://m.nicebizmap.co.kr"));
        
        // 디버그 모드
        envInfo.put("debugEnabled", environment.getProperty("custom.debug.enabled", Boolean.class, false));
        
        return envInfo;
    }
} 