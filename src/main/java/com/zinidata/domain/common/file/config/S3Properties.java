package com.zinidata.domain.common.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AWS S3 설정 Properties
 * 
 * application.yml의 custom.aws.s3 설정을 바인딩합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "custom.aws.s3")
@Getter
@Setter
public class S3Properties {
    
    /**
     * S3 버킷명
     */
    private String bucketName;
    
    /**
     * AWS 리전
     */
    private String region;
    
    /**
     * AWS 액세스 키
     */
    private String accessKey;
    
    /**
     * AWS 시크릿 키
     */
    private String secretKey;
    
    /**
     * Presigned URL 만료 시간 (초)
     */
    private int presignedUrlExpiration = 5;
    
    /**
     * 폴더 프리픽스
     */
    private String folderPrefix = "files/";
}