package com.zinidata.domain.common.locationsearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * 카카오 API 설정 클래스
 * 
 * application.yml의 kakao.location 하위 설정값들을 바인딩
 */
@Configuration
@ConfigurationProperties(prefix = "kakao.location")
@Getter
@Setter
public class KakaoApiConfig {
    
    /**
     * 카카오 REST API 키
     */
    private String apiKey;
    
    /**
     * API URL 설정
     */
    private ApiUrl apiUrl = new ApiUrl();
    
    /**
     * API URL 설정 클래스
     */
    @Getter
    @Setter
    public static class ApiUrl {
        /**
         * 주소 검색 API URL
         */
        private String address;
        
        /**
         * 키워드 검색 API URL  
         */
        private String keyword;
        
        /**
         * 좌표→주소 변환 API URL (역지오코딩)
         */
        private String coord2address;
    }
} 