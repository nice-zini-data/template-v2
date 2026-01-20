package com.zinidata.common.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class SecureHashAlgorithm {

    //-- 회원관리에 사용 ( 회원가입, 로그인 시 패스워드 암호화 ) - 암호화된 String값의 길이가 일정하지 않음
    public static String encryptSHA256(String pwd) throws NoSuchAlgorithmException {
        String encryptData = "";

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(pwd.getBytes());

        byte[] digest = sha.digest();
        for (int i = 0; i < digest.length; i++) {
            encryptData += Integer.toHexString(digest[i] & 0xFF).toUpperCase();
        }
        
        return encryptData;
    }

    //-- 외부에서 접속시 발급되는 토큰키 생성시 사용
    private static String _hexPseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    public static String encryptSHA_256(String pwd) throws NoSuchAlgorithmException {
        String encryptData = "";

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(pwd.getBytes());

        byte[] digest = sha.digest();
        for (int i = 0; i < digest.length; i++) {
            encryptData += Integer.toHexString(digest[i] & 0xFF).toUpperCase();
        }

        encryptData = convertByteArrayToHexString(digest);

        return encryptData;
    }

    public static String convertByteArrayToHexString(byte[] sourceBytes) {
        byte ch = 0x00;
        int nIndex = 0;

        if (sourceBytes == null || sourceBytes.length <= 0)
            return "";

        StringBuffer sbOut = new StringBuffer(sourceBytes.length * 2);

        while (nIndex < sourceBytes.length) {
            ch = (byte) (sourceBytes[nIndex] & 0xF0); // Strip Off high nibble
            ch = (byte) (ch >>> 4); // Strip the bits down
            ch = (byte) (ch & 0x0F); // high order bit is on
            sbOut.append(_hexPseudo[(int) ch]); // Convert the nibble to a
            // String Character
            ch = (byte) (sourceBytes[nIndex] & 0x0F); // Strip Off low nibble
            sbOut.append(_hexPseudo[(int) ch]); // Convert the nibble to a
            // String Character
            nIndex++;
        }

        return sbOut.toString();
    }
}