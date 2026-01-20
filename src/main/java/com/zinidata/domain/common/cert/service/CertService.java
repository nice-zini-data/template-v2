package com.zinidata.domain.common.cert.service;

import com.zinidata.domain.common.auth.mapper.AuthMapper;
import com.zinidata.domain.common.cert.vo.CertVO;
import com.zinidata.domain.common.sms.service.UnifiedSmsService;
import com.zinidata.security.ratelimit.service.CertRateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 문자인증 서비스
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertService {

    private final CertRateLimitService certRateLimitService;
    private final AuthMapper authMapper;
    private final UnifiedSmsService unifiedSmsService;
    

    /**
     * 인증번호 발송
     */
    public Map<String, Object> sendCertNumber(String mobileNo, String pathName, HttpServletRequest request, String memNm) throws Exception {
        log.info("인증번호 발송 처리 시작 - mobileNo: {}", mobileNo);

        CertVO certVo = new CertVO();

        // 회원가입 경로에서만 휴대폰 중복 가입 체크
        if ("/auth/signup".equals(pathName)) {
            if (authMapper.existsByMobileNo(mobileNo, certVo.getAppCode()) > 0) {
                throw new jakarta.validation.ValidationException("이미 가입된 휴대폰 번호입니다.");
            }
        }
        // 아이디 찾기 경로에서는 존재하는 휴대폰 번호여야 함
        else if ("/auth/findId".equals(pathName)) {
            if (authMapper.existsByMobileNo(mobileNo, certVo.getAppCode()) == 0) {
                throw new jakarta.validation.ValidationException("가입되지 않은 휴대폰 번호입니다.");
            }
        }

        // Rate Limit 체크 (1시간당 10회 제한)
        certRateLimitService.checkAndIncrement(mobileNo);

        // 6자리 랜덤 인증번호 생성
        String certNo = generateCertNumber();
        
        // 세션에 인증번호 저장
        HttpSession session = request.getSession();
        session.setAttribute("certNo_" + mobileNo, certNo);
        session.setAttribute("certTime_" + mobileNo, System.currentTimeMillis());
        
        // SMS 발송 처리 (실제 SMS 서비스 연동 부분)
        // SMS 발송 (통합 SMS 서비스 사용)
        boolean smsResult = unifiedSmsService.sendCertificationSms(mobileNo, certNo, memNm);
        
        if (!smsResult) {
            throw new RuntimeException("SMS 발송에 실패했습니다.");
        }
        
        // 응답 데이터 생성
        Map<String, Object> result = new HashMap<>();
        result.put("message", "인증번호가 발송되었습니다.");
        result.put("mobileNo", maskMobileNo(mobileNo));
        result.put("expireTime", 600); // 10분
        
        log.info("인증번호 발송 완료 - mobileNo: {}", mobileNo);
        return result;
    }

    /**
     * 인증번호 확인
     */
    public Map<String, Object> verifyCertNumber(String mobileNo, String certNo, HttpServletRequest request) throws Exception {
        log.info("인증번호 확인 처리 시작 - mobileNo: {}", mobileNo);

        HttpSession session = request.getSession();
        String sessionCertNo = (String) session.getAttribute("certNo_" + mobileNo);
        Long certTime = (Long) session.getAttribute("certTime_" + mobileNo);
        
        // 세션에서 인증번호 확인
        if (sessionCertNo == null || certTime == null) {
            throw new RuntimeException("인증번호가 발송되지 않았거나 만료되었습니다.");
        }
        
        // 10분(600초) 만료 체크
        long currentTime = System.currentTimeMillis();
        if (currentTime - certTime > 600000) { // 10분 = 600,000ms
            // 만료된 인증번호 정리
            session.removeAttribute("certNo_" + mobileNo);
            session.removeAttribute("certTime_" + mobileNo);
            throw new RuntimeException("인증번호가 만료되었습니다. 다시 발송해주세요.");
        }
        
        // 인증번호 일치 확인
        if (!sessionCertNo.equals(certNo)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }
        
        // 인증 성공 - 세션에 인증 상태 저장
        session.setAttribute("certVerified_" + mobileNo, true);
        session.setAttribute("certVerifiedTime_" + mobileNo, currentTime);
        
        // 사용된 인증번호 제거
        session.removeAttribute("certNo_" + mobileNo);
        session.removeAttribute("certTime_" + mobileNo);
        
        // 응답 데이터 생성
        Map<String, Object> result = new HashMap<>();
        result.put("message", "휴대폰 인증이 완료되었습니다.");
        result.put("mobileNo", maskMobileNo(mobileNo));
        result.put("verified", true);
        
        log.info("인증번호 확인 완료 - mobileNo: {}", mobileNo);
        return result;
    }

    /**
     * 6자리 랜덤 인증번호 생성
     */
    private String generateCertNumber() {
        Random random = new Random();
        int certNum = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(certNum);
    }

    /**
     * 휴대폰 번호 마스킹 처리
     */
    private String maskMobileNo(String mobileNo) {
        if (mobileNo == null || mobileNo.length() < 8) {
            return "****";
        }
        return mobileNo.substring(0, 3) + "****" + mobileNo.substring(mobileNo.length() - 4);
    }

}

