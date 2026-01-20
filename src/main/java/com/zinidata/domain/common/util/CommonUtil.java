package com.zinidata.domain.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommonUtil {

    
    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str) || str.toString().equals("[]");
    }

    public static String addComma(int number) {
        String formattedNumber = String.format("%,d", number);
        return formattedNumber;
    }

    public static String addComma(double number) {
        if (number == Math.floor(number)) {
            return String.format("%,.0f", number);
        } else {
            return String.format("%,.2f", number);
        }
    }

    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        char firstChar = name.charAt(0);
        char lastChar = name.charAt(name.length() - 1);
        StringBuilder maskedPart = new StringBuilder();
        for (int i = 1; i < name.length() - 1; i++) {
            maskedPart.append('*');
        }
        return firstChar + maskedPart.toString() + lastChar;
    }

    // ------------------ 유틸리티 메서드 정리 ------------------
    // 1. 값 비었는지 검사: isEmpty(Object str)
    // 2. 숫자에 천단위 콤마 추가: addComma(int/double number)
    // 3. 이름 마스킹: maskName(String name)
    // 4. 회원번호 조회(세션에서): getCurrentUserMemNo(HttpServletRequest request)
    // -----------------------------------------------------
    /**
     * 현재 로그인한 사용자의 회원번호 조회 (AuditLogService와 동일한 로직)
     * 
     * @param request HTTP 요청
     * @return 회원번호 (로그인되지 않은 경우 null)
     */
    public static Long getCurrentUserMemNo(HttpServletRequest request) {
        try {
            // Spring Security Authentication에서 사용자 정보 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                // HTTP 세션에서 memNo 추출
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Long memNo = (Long) session.getAttribute("memNo");
                    if (memNo != null) {
                        log.debug("세션에서 회원번호 추출: {}", memNo);
                        return memNo;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("사용자 회원번호 조회 실패", e);
        }
        
        return null;
    }
}
