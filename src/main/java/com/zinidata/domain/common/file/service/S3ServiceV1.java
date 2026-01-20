package com.zinidata.domain.common.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import com.zinidata.domain.common.file.config.S3Properties;
import com.zinidata.domain.common.file.exception.S3ServiceException;

import java.time.Duration;

/**
 * AWS S3 서비스
 * 
 * S3 파일 업로드/다운로드 및 Presigned URL 생성을 담당합니다.
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceV1 {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    
    /**
     * Presigned Download URL 생성 (5초 만료)
     * 
     * @param s3Key S3 객체 키
     * @return Presigned URL
     * @throws S3ServiceException S3 서비스 오류
     */
    public String generatePresignedDownloadUrl(String s3Key) {
        log.info("Presigned URL 생성 시작: s3Key={}, expiration={}초", 
                s3Key, s3Properties.getPresignedUrlExpiration());
        
        try {
            // 1. S3 객체 존재 확인
            if (!isObjectExists(s3Key)) {
                throw new S3ServiceException("S3 객체를 찾을 수 없습니다: " + s3Key);
            }
            
            // 2. GetObject 요청 생성
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(s3Key)
                    .build();
            
            // 3. Presigned URL 요청 생성
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(s3Properties.getPresignedUrlExpiration()))
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            // 4. Presigned URL 생성
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();
            
            log.info("Presigned URL 생성 완료: s3Key={}, url길이={}", s3Key, presignedUrl.length());
            return presignedUrl;
            
        } catch (S3ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: s3Key={}", s3Key, e);
            throw new S3ServiceException("Presigned URL 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * S3 객체 존재 여부 확인
     * 
     * @param s3Key S3 객체 키
     * @return 존재 여부
     */
    public boolean isObjectExists(String s3Key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(s3Key)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            log.warn("S3 객체가 존재하지 않음: s3Key={}", s3Key);
            return false;
        } catch (Exception e) {
            log.error("S3 객체 존재 확인 실패: s3Key={}", s3Key, e);
            return false;
        }
    }
    
    /**
     * S3 객체 메타데이터 조회
     * 
     * @param s3Key S3 객체 키
     * @return 객체 메타데이터
     * @throws S3ServiceException S3 서비스 오류
     */
    public HeadObjectResponse getObjectMetadata(String s3Key) {
        log.info("S3 객체 메타데이터 조회: s3Key={}", s3Key);
        
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(s3Key)
                    .build();
            
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            log.info("S3 객체 메타데이터 조회 완료: s3Key={}, size={}", s3Key, response.contentLength());
            
            return response;
            
        } catch (NoSuchKeyException e) {
            throw new S3ServiceException("S3 객체를 찾을 수 없습니다: " + s3Key);
        } catch (Exception e) {
            log.error("S3 객체 메타데이터 조회 실패: s3Key={}", s3Key, e);
            throw new S3ServiceException("S3 객체 메타데이터 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * S3 버킷 접근 가능 여부 확인
     * 
     * @return 접근 가능 여부
     */
    public boolean isBucketAccessible() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .build();
            
            s3Client.headBucket(headBucketRequest);
            log.info("S3 버킷 접근 가능: bucket={}", s3Properties.getBucketName());
            return true;
            
        } catch (Exception e) {
            log.error("S3 버킷 접근 불가: bucket={}", s3Properties.getBucketName(), e);
            return false;
        }
    }
}