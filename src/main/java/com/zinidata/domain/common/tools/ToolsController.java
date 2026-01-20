package com.zinidata.domain.common.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolsController {

    @Autowired
    @Qualifier("jasyptEncryptor")
    private StringEncryptor jasyptEncryptor;

    /**
     * Jasypt 암/복호화 도구 페이지
     */
    @GetMapping("/jasypt-encrypt")
    public String jasyptEncrypt(Model model) {
        model.addAttribute("pageTitle", "Jasypt 암/복호화 도구");
        return "tools/jasypt-encrypt";
    }

    /**
     * 암호화 API
     */
    @PostMapping("/jasypt/encrypt")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> encryptText(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String plainText = request.get("plainText");
            
            if (plainText == null || plainText.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "평문을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Jasypt 암호화 실행
            String encryptedText = jasyptEncryptor.encrypt(plainText);
            
            log.info("암호화 실행 - 평문 길이: {}, 암호화 결과 길이: {}", plainText.length(), encryptedText.length());
            
            response.put("success", true);
            response.put("encryptedText", encryptedText);
            response.put("message", "암호화 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("암호화 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "암호화 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 복호화 API
     */
    @PostMapping("/jasypt/decrypt")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> decryptText(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String encryptedText = request.get("encryptedText");
            
            if (encryptedText == null || encryptedText.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "암호화된 텍스트를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 입력값 검증
            String actualEncryptedText = encryptedText.trim();
            
            // ENC() 형태가 아닌 경우 경고
            if (!actualEncryptedText.startsWith("ENC(") || !actualEncryptedText.endsWith(")")) {
                response.put("success", false);
                response.put("message", "올바른 암호화 형식이 아닙니다. ENC(암호화된텍스트) 형태로 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // ENC() 제거하고 실제 암호화된 값 추출
            actualEncryptedText = actualEncryptedText.substring(4, actualEncryptedText.length() - 1);
            
            if (actualEncryptedText.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "암호화된 텍스트가 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Jasypt 복호화 실행
            String decryptedText = jasyptEncryptor.decrypt(actualEncryptedText);
            
            log.info("복호화 실행 - 암호화 텍스트 길이: {}, 복호화 결과 길이: {}", actualEncryptedText.length(), decryptedText.length());
            
            response.put("success", true);
            response.put("decryptedText", decryptedText);
            response.put("message", "복호화 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (org.jasypt.exceptions.EncryptionOperationNotPossibleException e) {
            log.error("복호화 실패 - 잘못된 암호화 텍스트: {}", request.get("encryptedText"), e);
            response.put("success", false);
            response.put("message", "복호화할 수 없는 텍스트입니다. 올바른 암호화된 텍스트인지 확인해주세요.");
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            log.error("복호화 실패 - 잘못된 형식: {}", request.get("encryptedText"), e);
            response.put("success", false);
            response.put("message", "암호화된 텍스트 형식이 올바르지 않습니다. ENC(암호화된텍스트) 형태로 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("복호화 중 예상치 못한 오류 발생", e);
            response.put("success", false);
            response.put("message", "복호화 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API 테스트 엔드포인트
     */
    @GetMapping("/jasypt/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testJasypt() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String testText = "Hello, Jasypt!";
            
            // 암호화 테스트
            String encrypted = jasyptEncryptor.encrypt(testText);
            log.info("테스트 암호화: {} -> {}", testText, encrypted);
            
            // 복호화 테스트
            String decrypted = jasyptEncryptor.decrypt(encrypted);
            log.info("테스트 복호화: {} -> {}", encrypted, decrypted);
            
            boolean isWorking = testText.equals(decrypted);
            
            response.put("success", isWorking);
            response.put("testText", testText);
            response.put("encrypted", encrypted);
            response.put("decrypted", decrypted);
            response.put("message", isWorking ? "Jasypt 정상 동작" : "Jasypt 동작 오류");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Jasypt 테스트 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "Jasypt 테스트 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}