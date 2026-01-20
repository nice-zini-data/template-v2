package com.zinidata.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * AES 256 암/복호화 유틸리티
 */
@Slf4j
@Component
public class AesCryptoUtil {
    
    private static String aes256Key;
    
    @Value("${key.aes256Key}")
    public void setAes256Key(String key) {
        AesCryptoUtil.aes256Key = key;
    }

    public static String getAes256Key() {
        if (aes256Key == null) {
            // fallback to hardcoded key if not set
            return "3f9be62dea07a54b18cd908e5b4f23aa";
        }
        return aes256Key;
    }

     // AES 256 암호화
     public static String encrypt(String target){
        try {
            Key secretKey = new SecretKeySpec(getAes256Key().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(target.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }catch (Exception e){
            return e.toString();
        }
    }

    // AES 256 복호화
    public static String decrypt(String target) {
        try {
            Key secretKey = new SecretKeySpec(getAes256Key().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(target);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e){
            return e.toString();
        }
    }
}
