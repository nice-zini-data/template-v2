package com.zinidata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스
 * 
 * <p>외부 API 호출을 위한 RestTemplate 빈을 설정합니다.</p>
 * <p>카카오 OAuth API 등 외부 서비스 연동에 사용됩니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 기본 RestTemplate 빈
     * 
     * @return 설정된 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 타임아웃 설정 (밀리초)
        factory.setConnectTimeout(10000);  // 연결 타임아웃: 10초
        factory.setReadTimeout(30000);     // 읽기 타임아웃: 30초
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        return restTemplate;
    }
}
