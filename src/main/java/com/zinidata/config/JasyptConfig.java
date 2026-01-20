package com.zinidata.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt 암호화 설정
 * BouncyCastle Provider 사용
 */
@Configuration
public class JasyptConfig {

    /**
     * Jasypt 문자열 암호화 빈
     */
    @Bean("jasyptEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setPoolSize(2);
        encryptor.setAlgorithm("PBEWithSHA256And128BitAES-CBC-BC");
        
        // 환경변수에서 암호화 키 가져오기 (표준 방식)
        String password = getEncryptorPassword();
        encryptor.setPassword(password);

        return encryptor;
    }
    
    /**
     * 암호화 키를 환경변수에서 가져오기
     * 
     * @return 암호화 키
     */
    private String getEncryptorPassword() {
        // 1. 환경변수에서 확인 (권장)
        String password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        
        // 2. 시스템 프로퍼티에서 확인
        if (password == null) {
            password = System.getProperty("JASYPT_ENCRYPTOR_PASSWORD");
        }
        
        // 3. 기본값 (개발 단계용 - 운영에서는 반드시 환경변수 설정 필요)
        if (password == null) {
            password = "wlslepdlxk0904!@#";
            System.out.println("⚠️ 경고: 환경변수 JASYPT_ENCRYPTOR_PASSWORD가 설정되지 않아 기본값을 사용합니다.");
            System.out.println("💡 권장: export JASYPT_ENCRYPTOR_PASSWORD=\"wlslepdlxk0904!@#\"");
        }
        
        return password;
    }
} 