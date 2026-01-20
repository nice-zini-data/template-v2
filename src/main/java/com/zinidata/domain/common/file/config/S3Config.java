package com.zinidata.domain.common.file.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 설정
 * 
 * S3Client와 S3Presigner Bean을 생성합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3Config {
    
    private final S3Properties s3Properties;
    
    /**
     * S3 클라이언트 Bean 생성
     * 
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        log.info("S3Client 초기화: region={}, bucket={}", 
                s3Properties.getRegion(), s3Properties.getBucketName());
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            s3Properties.getAccessKey(),
            s3Properties.getSecretKey()
        );
        
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
    
    /**
     * S3 Presigner Bean 생성
     * 
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        log.info("S3Presigner 초기화: region={}", s3Properties.getRegion());
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            s3Properties.getAccessKey(),
            s3Properties.getSecretKey()
        );
        
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}