package com.zinidata.domain.common.auth.service;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.common.util.AesCryptoUtil;
import com.zinidata.common.util.SecureHashAlgorithm;
import com.zinidata.domain.common.auth.mapper.AuthMapper;
import com.zinidata.domain.common.auth.vo.MemberVO;
import com.zinidata.domain.common.util.CommonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 인증 관련 비즈니스 서비스
 * 
 * <p><strong>📌 신규 기능:</strong></p>
 * <ul>
 *   <li>✅ <strong>아이디 존재 여부 확인:</strong> checkUserExists()</li>
 *   <li>✅ <strong>자동 회원가입:</strong> register()</li>
 *   <li>✅ <strong>로그인 처리:</strong> login()</li>
 * </ul>
 * 
 * <p><strong>🔗 기존 기능 유지:</strong></p>
 * <ul>
 *   <li>회원가입, 중복체크, 세션조회 등</li>
 *   <li>아이디/비밀번호 찾기</li>
 * </ul>
 * 
 * @author ZiniData 개발팀
 * @since 2.0
 */
@Slf4j
@Service("authService")
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper authMapper;
    private final RestTemplate restTemplate;

    @Value("${app.code:NBZM}")
    private String appCode;
    
    @Value("${nibs.api.url:https://nibs.nicevan.co.kr}")
    private String nibsApiUrl;

    /**
     * 🟢 아이디 존재 여부 확인
     */
    public boolean checkUserExists(MemberVO requestVo) throws Exception {

        // 아이디 존재 여부 확인
        MemberVO member = authMapper.findByLoginId(requestVo);
        boolean exists = (member != null);
        
        return exists;
    }

    /**
     * 🟢 자동 회원가입
     */
    public Map<String, Object> register(MemberVO requestVo) throws Exception {
 
        requestVo.setPassword(SecureHashAlgorithm.encryptSHA256(requestVo.getLoginId()));
        
        // 입력값 검증
        validateRegisterInput(requestVo);
        
        // 아이디 중복 체크
        validateAndCheckLoginIdDuplicate(requestVo);
        
        // 비밀번호 해시화
        String hashedPassword = SecureHashAlgorithm.encryptSHA256(requestVo.getLoginId());
        requestVo.setPassword(hashedPassword);

        // 회원번호 가져오기 (tb_member_prj에도 사용하기 위함)
        Long memNo = authMapper.getMemberSeq();
        requestVo.setMemNo(memNo);
        
        log.info(requestVo.getMemNm());
        log.info(AesCryptoUtil.encrypt(requestVo.getMemNm()));
        // 회원정보 암호화 이름, 전화번호
        requestVo.setMemNm(AesCryptoUtil.encrypt(requestVo.getMemNm()));
        requestVo.setMobileNo(AesCryptoUtil.encrypt(requestVo.getMobileNo()));

        // 회원가입 처리
        int result = authMapper.insertMember(requestVo);
        
        if (result <= 0) {
            throw new Exception("회원가입 처리 중 오류가 발생했습니다.");
        }
        
        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("memNo", memNo);
        responseData.put("loginId", requestVo.getLoginId());
        responseData.put("memNm", requestVo.getMemNm());
        responseData.put("mobileNo", requestVo.getMobileNo());
        
        log.info("[AUTH] 자동 회원가입 완료 - memNo: {}, loginId: {}", memNo, requestVo.getLoginId());
        return responseData;
    }

    /**
     * 🟢 로그인 처리
     */
    public Map<String, Object> login(MemberVO requestVo, HttpServletRequest request) throws Exception {
        log.info("[AUTH] 로그인 처리 시작 - loginId: {}", requestVo.getLoginId());
        
        // 입력값 검증
        if (requestVo.getLoginId() == null || requestVo.getLoginId().trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "아이디를 입력해주세요.");
        }

        // 사용자 조회
        MemberVO member = authMapper.findByLoginId(requestVo);
        if (member == null) {
            throw new ValidationException(Status.데이터없음, "존재하지 않는 아이디입니다.");
        }

        // 사용자 정보 API 호출 정보로 무조건 업데이트 하기 (이름, 전화번호 등 정보 변경 됐을때 대응)
        requestVo.setMemNm(AesCryptoUtil.encrypt(requestVo.getMemNm()));
        requestVo.setMobileNo(AesCryptoUtil.encrypt(requestVo.getMobileNo()));
        requestVo.setMemNo(member.getMemNo());
        int result = authMapper.updateMember(requestVo);
        if (result <= 0) {
            throw new Exception("사용자 정보 업데이트 처리 중 오류가 발생했습니다.");
        }

        // 이름, 전화번호 복호화
        member.setMemNm(AesCryptoUtil.decrypt(requestVo.getMemNm()));
        member.setMobileNo(AesCryptoUtil.decrypt(requestVo.getMobileNo()));
        
        // 세션 생성
        HttpSession session = request.getSession(true);
        session.setAttribute("memNo", member.getMemNo());
        session.setAttribute("loginId", member.getLoginId());
        session.setAttribute("name", member.getMemNm());
        session.setAttribute("memType", member.getMemType());
        session.setAttribute("emailAddr", member.getEmailAddr());
        session.setAttribute("mobileNo", member.getMobileNo());
        session.setAttribute("memStat", member.getMemStat());
        session.setAttribute("authCd", member.getAuthCd());
        
        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("memNo", member.getMemNo());
        responseData.put("loginId", member.getLoginId());
        responseData.put("memNm", member.getMemNm());
        responseData.put("memType", member.getMemType());
        responseData.put("emailAddr", member.getEmailAddr());
        responseData.put("mobileNo", member.getMobileNo());
        responseData.put("memStat", member.getMemStat());
        responseData.put("sessionId", session.getId());
        responseData.put("authCd", member.getAuthCd());
        responseData.put("loginTimestamp", System.currentTimeMillis());
        
        log.info("[AUTH] 로그인 성공 - memNo: {}, loginId: {}, mobileNo: {}, authCd: {}, sessionId: {}",
                member.getMemNo(), member.getLoginId(), member.getMobileNo(), member.getAuthCd(), session.getId());

        // 로그인 이력 insert
        try {
            String ipAddr = getClientIpAddress(request);
            int logResult = authMapper.insertLogAuth(member.getMemNo(), session.getId(), ipAddr);
            if (logResult > 0) {
                log.info("[AUTH] 로그인 이력 저장 완료 - memNo: {}, sessionId: {}, ipAddr: {}", 
                        member.getMemNo(), session.getId(), ipAddr);
            }
        } catch (Exception e) {
            log.error("[AUTH] 로그인 이력 저장 중 오류 발생 - memNo: {}", member.getMemNo(), e);
            // 로그인 이력 저장 실패해도 로그인은 계속 진행
        }
        
        return responseData;
    }

    /**
     * 회원가입 입력값 검증
     */
    private void validateRegisterInput(MemberVO requestVo) throws ValidationException {
        
        if (requestVo.getLoginId() == null || requestVo.getLoginId().trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "아이디를 입력해주세요.");
        }
        
        if (requestVo.getPassword() == null || requestVo.getPassword().trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "비밀번호를 입력해주세요.");
        }
        
        if (requestVo.getPassword().length() < 8) {
            throw new ValidationException(Status.파라미터오류, "비밀번호는 8자 이상이어야 합니다.");
        }
        
        // 아이디 형식 검증
        if (!requestVo.getLoginId().matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new ValidationException(Status.파라미터오류, "아이디는 영문, 숫자 4-20자로 입력해주세요.");
        }
        
        // 전화번호 형식 검증 (전화번호가 있는 경우)
        if (requestVo.getMobileNo() != null && !requestVo.getMobileNo().trim().isEmpty()) {
            if (!requestVo.getMobileNo().matches("^01[0-9]\\d{3,4}\\d{4}$")) {
                throw new ValidationException(Status.파라미터오류, "올바른 전화번호 형식을 입력해주세요. (예: 01012345678)");
            }
        }
    }

    /**
     * 🟢 아이디 중복 체크
     */
    public void validateAndCheckLoginIdDuplicate(String loginId) throws Exception {
        log.info("[AUTH] 아이디 중복 체크 - loginId: {}", loginId);
        
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "아이디를 입력해주세요.");
        }
        
        // 아이디 형식 검증
        if (!loginId.matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new ValidationException(Status.파라미터오류, "아이디는 영문, 숫자 4-20자로 입력해주세요.");
        }
        
        // 중복 체크
        MemberVO memberVO = new MemberVO();
        memberVO.setLoginId(loginId.trim());
        memberVO.setAppCode(appCode);
        
        MemberVO existingMember = authMapper.findByLoginId(memberVO);
        if (existingMember != null) {
            throw new ValidationException(Status.아이디중복, "이미 사용 중인 아이디입니다.");
        }
        
        log.info("[AUTH] 아이디 중복 체크 완료 - 사용 가능: {}", loginId);
    }

    /**
     * 🟢 아이디 중복 체크 (MemberVO 사용)
     */
    public void validateAndCheckLoginIdDuplicate(MemberVO requestVo) throws Exception {
        log.info("[AUTH] 아이디 중복 체크 - loginId: {}", requestVo.getLoginId());
        
        if (CommonUtil.isEmpty(requestVo.getLoginId())) {
            throw new ValidationException(Status.파라미터오류, "아이디를 입력해주세요.");
        }
        
        // 아이디 형식 검증
        if (!requestVo.getLoginId().matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new ValidationException(Status.파라미터오류, "아이디는 영문, 숫자 4-20자로 입력해주세요.");
        }
        
        // 중복 체크
        MemberVO existingMember = authMapper.findByLoginId(requestVo);
        if (existingMember != null) {
            throw new ValidationException(Status.아이디중복, "이미 사용 중인 아이디입니다.");
        }
        
        log.info("[AUTH] 아이디 중복 체크 완료 - 사용 가능: {}", requestVo.getLoginId());
    }

    /**
     * 🟢 이메일 중복 체크
     */
    public void validateAndCheckEmailDuplicate(String emailAddr) throws Exception {
        log.info("[AUTH] 이메일 중복 체크 - emailAddr: {}", emailAddr);
        
        if (emailAddr == null || emailAddr.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "이메일을 입력해주세요.");
        }
        
        // 이메일 형식 검증
        if (!emailAddr.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new ValidationException(Status.파라미터오류, "올바른 이메일 형식을 입력해주세요.");
        }
        
        // 중복 체크
        MemberVO existingMember = authMapper.findByEmailAddr(emailAddr.trim());
        if (existingMember != null) {
            throw new ValidationException(Status.이메일중복, "이미 사용 중인 이메일입니다.");
        }
        
        log.info("[AUTH] 이메일 중복 체크 완료 - 사용 가능: {}", emailAddr);
    }

    /**
     * 🟢 회원가입
     */
    public Map<String, Object> signup(MemberVO requestVo) throws Exception {
        log.info("[AUTH] 회원가입 시작");
        
        // 입력값 검증
        validateSignupInput(requestVo);
        
        // 아이디 중복 체크
        validateAndCheckLoginIdDuplicate(requestVo);
        
        // 비밀번호 해시화
        String hashedPassword = SecureHashAlgorithm.encryptSHA256(requestVo.getLoginId());
        requestVo.setPassword(hashedPassword);
        
        // 회원번호 가져오기
        Long memNo = authMapper.getMemberSeq();
        requestVo.setMemNo(memNo);
        
        // 회원가입 처리
        int result = authMapper.insertMember(requestVo);
        
        if (result <= 0) {
            throw new Exception("회원가입 처리 중 오류가 발생했습니다.");
        }
        
        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("memNo", requestVo.getMemNo());
        responseData.put("loginId", requestVo.getLoginId());
        responseData.put("memNm", requestVo.getMemNm());
        responseData.put("emailAddr", requestVo.getEmailAddr());
        responseData.put("mobileNo", requestVo.getMobileNo());
        
        log.info("[AUTH] 회원가입 완료 - memNo: {}, loginId: {}", requestVo.getMemNo(), requestVo.getLoginId());
        return responseData;
    }

    /**
     * 회원가입 입력값 검증
     */
    private void validateSignupInput(MemberVO requestVo) throws ValidationException {
        
        if (CommonUtil.isEmpty(requestVo.getLoginId())) {
            throw new ValidationException(Status.파라미터오류, "아이디를 입력해주세요.");
        }
        
        if (CommonUtil.isEmpty(requestVo.getPassword())) {
            throw new ValidationException(Status.파라미터오류, "비밀번호를 입력해주세요.");
        }
        
        if (requestVo.getPassword().length() < 8) {
            throw new ValidationException(Status.파라미터오류, "비밀번호는 8자 이상이어야 합니다.");
        }
        
        if (CommonUtil.isEmpty(requestVo.getMemNm())) {
            throw new ValidationException(Status.파라미터오류, "이름을 입력해주세요.");
        }
        
        // 아이디 형식 검증
        if (requestVo.getLoginId().matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new ValidationException(Status.파라미터오류, "아이디는 영문, 숫자 4-20자로 입력해주세요.");
        }
        
        // 이메일 형식 검증 (이메일이 있는 경우)
        if(!CommonUtil.isEmpty(requestVo.getEmailAddr())){
            if (!requestVo.getEmailAddr().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                throw new ValidationException(Status.파라미터오류, "올바른 이메일 형식을 입력해주세요.");
            }
        }
        
        // 전화번호 형식 검증 (전화번호가 있는 경우)
        if (!CommonUtil.isEmpty(requestVo.getMobileNo())) {
            if (!requestVo.getMobileNo().matches("^01[0-9]-\\d{3,4}-\\d{4}$")) {
                throw new ValidationException(Status.파라미터오류, "올바른 전화번호 형식을 입력해주세요. (예: 010-1234-5678)");
            }
        }
    }

    /**
     * 🟢 아이디 찾기
     */
    public Map<String, Object> findId(MemberVO requestVo) throws Exception {
        log.info("[AUTH] 아이디 찾기");
        
        // 아이디 찾기
        MemberVO member = authMapper.findByMemNmAndMobileNo(requestVo);
        if (member == null) {
            throw new ValidationException(Status.데이터없음, "입력하신 정보와 일치하는 회원이 없습니다.");
        }
        
        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("loginId", requestVo.getLoginId());
        responseData.put("memNm", requestVo.getMemNm());
        responseData.put("emailAddr", requestVo.getEmailAddr());
        
        log.info("[AUTH] 아이디 찾기 완료 - loginId: {}", requestVo.getLoginId());
        return responseData;
    }

    /**
     * 🟢 비밀번호 찾기
     */
    public Map<String, Object> findPassword(MemberVO requestVo, HttpServletRequest request) throws Exception {
        log.info("[AUTH] 비밀번호 찾기");
        
        // 입력값 검증
        if (CommonUtil.isEmpty(requestVo.getLoginId())) {
            throw new ValidationException(Status.파라미터오류, "아이디를 입력해주세요.");
        }
        
        if (CommonUtil.isEmpty(requestVo.getMobileNo())) {
            throw new ValidationException(Status.파라미터오류, "휴대폰 번호를 입력해주세요.");
        }
        
        // 사용자 조회
        MemberVO member = authMapper.findByLoginIdAndMobileNo(requestVo);
        if (member == null) {
            throw new ValidationException(Status.데이터없음, "입력하신 정보와 일치하는 회원이 없습니다.");
        }
        
        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();
        String hashedTempPassword = SecureHashAlgorithm.encryptSHA256(tempPassword);
        
        // 임시 비밀번호로 업데이트
        member.setPassword(hashedTempPassword);
        int result = authMapper.updatePassword(member.getMemNo(), hashedTempPassword);
        
        if (result <= 0) {
            throw new Exception("임시 비밀번호 생성 중 오류가 발생했습니다.");
        }
        
        // 세션에 임시 비밀번호 변경 권한 저장
        HttpSession session = request.getSession(true);
        session.setAttribute("tempPasswordChangeAuth", true);
        session.setAttribute("tempPasswordMemNo", member.getMemNo());
        
        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("tempPassword", tempPassword);
        responseData.put("memNo", member.getMemNo());
        responseData.put("loginId", member.getLoginId());
        responseData.put("memNm", member.getMemNm());
        
        log.info("[AUTH] 비밀번호 찾기 완료 - loginId: {}", member.getLoginId());
        return responseData;
    }

    /**
     * 🟢 비밀번호 변경
     */
    public Map<String, Object> changePassword(MemberVO requestVo, HttpServletRequest request) throws Exception {
        log.info("[AUTH] 비밀번호 변경 요청");
        
        // 세션에서 임시 비밀번호 변경 권한 확인
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ValidationException(Status.파라미터오류, "세션이 만료되었습니다.");
        }
        
        Boolean tempPasswordChangeAuth = (Boolean) session.getAttribute("tempPasswordChangeAuth");
        Long tempPasswordMemNo = (Long) session.getAttribute("tempPasswordMemNo");
        
        if (tempPasswordChangeAuth == null || !tempPasswordChangeAuth || tempPasswordMemNo == null) {
            throw new ValidationException(Status.파라미터오류, "비밀번호 변경 권한이 없습니다.");
        }
        
        // 새 비밀번호 검증
        if (CommonUtil.isEmpty(requestVo.getNewPassword())) {
            throw new ValidationException(Status.파라미터오류, "새 비밀번호를 입력해주세요.");
        }
        
        if (requestVo.getNewPassword().length() < 8) {
            throw new ValidationException(Status.파라미터오류, "비밀번호는 8자 이상이어야 합니다.");
        }
        
        // 새 비밀번호 해시화
        String hashedNewPassword = SecureHashAlgorithm.encryptSHA256(requestVo.getNewPassword());
        requestVo.setNewPassword(hashedNewPassword);

        // 비밀번호 변경
        int result = authMapper.updatePassword(tempPasswordMemNo, hashedNewPassword);
        
        if (result <= 0) {
            throw new Exception("비밀번호 변경 중 오류가 발생했습니다.");
        }
        
        // 세션에서 임시 비밀번호 변경 권한 제거
        session.removeAttribute("tempPasswordChangeAuth");
        session.removeAttribute("tempPasswordMemNo");
        
        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("memNo", tempPasswordMemNo);
        responseData.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        
        log.info("[AUTH] 비밀번호 변경 완료 - memNo: {}", tempPasswordMemNo);
        return responseData;
    }

    /**
     * 임시 비밀번호 생성
     */
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder tempPassword = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return tempPassword.toString();
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 여러 IP가 있는 경우 첫 번째 사용
                int commaIndex = ip.indexOf(',');
                if (commaIndex != -1) {
                    ip = ip.substring(0, commaIndex).trim();
                }
                return ip;
            }
        }
        
        // 헤더에서 찾지 못한 경우 기본 remote address 사용
        return request.getRemoteAddr();
    }

    /**
     * 🟢 NIBS 계약 로그인 API 호출
     * 
     * @param userId 사용자 ID
     * @param pwd 비밀번호
     * @param userName 사용자 이름
     * @param phoneNumber 휴대폰 번호
     * @param request HttpServletRequest (클라이언트 IP 추출용)
     * @return API 응답 결과 (resData, resCode 포함, isMaster 포함)
     */
    public Map<String, Object> callNibsContractLogin(String userId, String pwd, String userName, String phoneNumber, HttpServletRequest request) throws Exception {
        String clientIp = getClientIpAddress(request);
        Map<String, Object> result = callNibsContractLoginApi(userId, pwd, userName, phoneNumber, clientIp);
        
        // isMaster 값 확인하여 authCd 결정
        Boolean isMaster = (Boolean) result.get("isMaster");
        String authCd = (isMaster != null && isMaster) ? "AUTH110" : "AUTH100";
        
        // 로그인 아이디 체크 - 아이디가 있으면 로그인 정보 return
        MemberVO checkVo = new MemberVO();
        checkVo.setLoginId(userId);
        checkVo.setAppCode(appCode);
        boolean userExists = checkUserExists(checkVo);
        
        // resData에서 NVPS 정보 추출
        Map<String, Object> resData = (Map<String, Object>) result.get("resData");
        Map<String, Object> nvpsInfo = null;
        if (resData != null) {
            nvpsInfo = new HashMap<>();
            nvpsInfo.put("userCode", resData.get("userCode"));
            nvpsInfo.put("agentName", resData.get("agentName"));
            nvpsInfo.put("agentCode", resData.get("agentCode"));
            nvpsInfo.put("pAgentCode", resData.get("pAgentCode"));
            nvpsInfo.put("pgAgentCode", resData.get("pgAgentCode"));
            nvpsInfo.put("pPgAgentCode", resData.get("pPgAgentCode"));
            nvpsInfo.put("agentBusinessNo", resData.get("agentBusinessNo"));
        }
        
        if (userExists) {
            // 로그인 처리
            MemberVO loginVo = new MemberVO();
            loginVo.setLoginId(userId);
            loginVo.setMemNm(userName);
            loginVo.setMobileNo(phoneNumber);
            loginVo.setAuthCd(authCd); // isMaster 값에 따라 AUTH110 또는 AUTH100
            loginVo.setAppCode(appCode); // 프로젝트 코드 설정
            Map<String, Object> loginResult = login(loginVo, request);
            result.putAll(loginResult);
            
            // tb_members_nvps 업데이트
            if (nvpsInfo != null && loginResult.get("memNo") != null) {
                try {
                    Long memNo = ((Number) loginResult.get("memNo")).longValue();
                    int updateResult = authMapper.updateMemberNvps(memNo, nvpsInfo);
                    if (updateResult > 0) {
                        log.info("[AUTH] NIBS 계약 로그인 - tb_members_nvps 업데이트 완료 - memNo: {}", memNo);
                    } else {
                        // 업데이트 실패 시 insert 시도 (레코드가 없는 경우)
                        int insertResult = authMapper.insertMemberNvps(memNo, nvpsInfo);
                        if (insertResult > 0) {
                            log.info("[AUTH] NIBS 계약 로그인 - tb_members_nvps insert 완료 - memNo: {}", memNo);
                        }
                    }
                } catch (Exception e) {
                    log.error("[AUTH] NIBS 계약 로그인 - tb_members_nvps 처리 중 오류 발생 - memNo: {}", loginResult.get("memNo"), e);
                    // 오류 발생해도 로그인은 계속 진행
                }
            }
            
            log.info("[AUTH] NIBS 계약 로그인 - 기존 사용자 로그인 완료 - loginId: {}, authCd: {}, isMaster: {}", userId, authCd, isMaster);
        } else {
            // 회원가입 처리 후 로그인 정보 return
            MemberVO registerVo = new MemberVO();
            registerVo.setLoginId(userId);
            registerVo.setMemNm(userName);
            registerVo.setMobileNo(phoneNumber);
            registerVo.setAuthCd(authCd); // isMaster 값에 따라 AUTH110 또는 AUTH100
            registerVo.setAppCode(appCode); // 프로젝트 코드 설정
            Map<String, Object> registerResult = register(registerVo);
            log.info("[AUTH] NIBS 계약 로그인 - 회원가입 완료 - loginId: {}, authCd: {}, isMaster: {}", userId, authCd, isMaster);
            
            // tb_members_nvps insert
            if (nvpsInfo != null && registerResult.get("memNo") != null) {
                try {
                    Long memNo = ((Number) registerResult.get("memNo")).longValue();
                    int insertResult = authMapper.insertMemberNvps(memNo, nvpsInfo);
                    if (insertResult > 0) {
                        log.info("[AUTH] NIBS 계약 로그인 - tb_members_nvps insert 완료 - memNo: {}", memNo);
                    }
                } catch (Exception e) {
                    log.error("[AUTH] NIBS 계약 로그인 - tb_members_nvps insert 중 오류 발생 - memNo: {}", registerResult.get("memNo"), e);
                    // 오류 발생해도 회원가입은 계속 진행
                }
            }
            
            // 회원가입 후 로그인 처리
            MemberVO loginVo = new MemberVO();
            loginVo.setLoginId(userId);
            loginVo.setMemNm(userName);
            loginVo.setMobileNo(phoneNumber);
            loginVo.setAuthCd(authCd); // isMaster 값에 따라 AUTH110 또는 AUTH100
            loginVo.setAppCode(appCode); // 프로젝트 코드 설정
            Map<String, Object> loginResult = login(loginVo, request);
            result.putAll(loginResult);
            log.info("[AUTH] NIBS 계약 로그인 - 신규 사용자 회원가입 및 로그인 완료 - loginId: {}, authCd: {}, isMaster: {}", userId, authCd, isMaster);
        }
        
        return result;
    }

    /**
     * 🟢 NIBS 계약 로그인 API 호출
     * 
     * @param userId 사용자 ID
     * @param pwd 비밀번호
     * @param userName 사용자 이름
     * @param phoneNumber 휴대폰 번호
     * @param clientIp 클라이언트 IP 주소
     * @return API 응답 결과 (resData, resCode 포함, isMaster 포함)
     */
    public Map<String, Object> callNibsContractLoginApi(String userId, String pwd, String userName, String phoneNumber, String clientIp) throws Exception {
        log.info("[AUTH] NIBS 계약 로그인 API 호출 시작 - userId: {}, userName: {}, clientIp: {}, phoneNumber: {}", userId, userName, clientIp, phoneNumber);
        
        try {
            String url = nibsApiUrl + "/login/contractLogin.do";
            
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "nice_cont");
            
            // 요청 body 생성
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("pwd", pwd);
            userInfo.put("clientIp", clientIp);
            userInfo.put("nVanFlag", "Y");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userInfo", userInfo);
            
            log.info("[AUTH] NIBS 계약 로그인 API 호출 - url: {}, userId: {}, userName: {}, phoneNumber: {}", url, userId, userName, phoneNumber);
            
            Map<String, Object> responseBody;
            Map<String, Object> resData;
            Map<String, Object> resCode;
            
            // ============================================
            // 실제 API 호출 사용 시: 아래 주석 해제, 하드코딩 부분 주석 처리
            // ============================================
            
            /*HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            // API 호출
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            responseBody = response.getBody();
            resData = (Map<String, Object>) responseBody.get("resData");
            resCode = (Map<String, Object>) responseBody.get("resCode");
            
            log.info("[AUTH] NIBS 계약 로그인 API 호출 성공 (실제 API) - 응답: {}", responseBody);*/
           
            
            // ============================================
            // 하드코딩 사용 시: 아래 주석 해제, 실제 API 호출 부분 주석 처리
            // ============================================
             Map<String, Object> nimoInfo = new HashMap<>();
            nimoInfo.put("userId", userId);
            nimoInfo.put("userName", userName);
            nimoInfo.put("groupCode", "A");
            nimoInfo.put("businessNo", "");
            nimoInfo.put("deptCode", "");
            nimoInfo.put("agentCode", "3800");
            nimoInfo.put("agentName", "Direct-VAN");
            nimoInfo.put("parentAgentYn", "Y");
            nimoInfo.put("mobileNo", phoneNumber);
            nimoInfo.put("parentAgentCode", "");
            nimoInfo.put("termId", "01");
            nimoInfo.put("userIp", "112.222.97.132");
            nimoInfo.put("prohibition", "N");
            nimoInfo.put("dutyCode", "99");
            
            resData = new HashMap<>();
            resData.put("userId", userId);
            resData.put("userName", userName);
            resData.put("userCode", "B");
            resData.put("agentBusinessTel", "1833-4170");
            resData.put("agentUsrPhone", "01073041558");
            resData.put("agentEmail", "family@nicevan.co.kr");
            resData.put("agentCode", "3800");
            resData.put("pAgentCode", "3800");
            resData.put("agentName", "Direct-VAN");
            resData.put("partnerCode", "");
            resData.put("agentBusinessNo", "2208115770");
            resData.put("agentDirectorName", "고병권");
            resData.put("agentAddr1", "서울특별시 영등포구 은행로 17 (여의도동)");
            resData.put("agentAddr2", "3층 나이스정보통신");
            resData.put("lastDateTime", "20260114 092811");
            resData.put("pgAgentCode", "");
            resData.put("pPgAgentCode", "");
            resData.put("nimoInfo", nimoInfo);
            
            resCode = new HashMap<>();
            resCode.put("errorCode", "LGI_0000");
            resCode.put("errorMsg", "로그인 성공!");
            
            responseBody = new HashMap<>();
            responseBody.put("resData", resData);
            responseBody.put("resCode", resCode);
            
            log.info("[AUTH] NIBS 계약 로그인 API 호출 성공 (하드코딩) - 응답: {}", responseBody); 
            
            // 응답 구조 확인
            Map<String, Object> result = new HashMap<>();
            
            // resCode 확인
            result.put("resCode", resCode);
            
            String errorCode = (String) resCode.get("errorCode");
            String errorMsg = (String) resCode.get("errorMsg");
            
            log.info("[AUTH] NIBS 응답 - errorCode: {}, errorMsg: {}", errorCode, errorMsg);
            
            // 에러 코드가 있으면 예외 발생
            if (errorCode != null && !errorCode.isEmpty() && !"0000".equals(errorCode) && !"LGI_0000".equals(errorCode)) {
                throw new ValidationException(Status.데이터없음, errorMsg != null ? errorMsg : "로그인에 실패했습니다.");
            }
            
            // resData 확인
            result.put("resData", resData);
            
            // agentUsrPhone과 phoneNumber, userName 비교하여 master 권한 확인
            boolean isMaster = false;
            
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                String agentUsrPhone = (String) resData.get("agentUsrPhone");
                String resUserName = (String) resData.get("userName");
                
                if (agentUsrPhone != null && !agentUsrPhone.trim().isEmpty() && 
                    resUserName != null && !resUserName.trim().isEmpty()) {
                    
                    // 휴대폰 번호에서 하이픈 제거하여 비교
                    String normalizedPhoneNumber = phoneNumber.replaceAll("-", "").trim();
                    String normalizedAgentUsrPhone = agentUsrPhone.replaceAll("-", "").trim();
                    
                    // 이름 비교 (공백 제거)
                    String normalizedUserName = userName.trim();
                    String normalizedResUserName = resUserName.trim();
                    
                    // phoneNumber와 userName 둘 다 같을 때만 isMaster = true
                    // 전화번호는 뒤에서 4자리가 같으면 일치로 판단
                    boolean phoneMatch = false;
                    if (normalizedPhoneNumber.length() >= 4 && normalizedAgentUsrPhone.length() >= 4) {
                        String last4Phone = normalizedPhoneNumber.substring(normalizedPhoneNumber.length() - 4);
                        String last4AgentPhone = normalizedAgentUsrPhone.substring(normalizedAgentUsrPhone.length() - 4);
                        phoneMatch = last4Phone.equals(last4AgentPhone);
                    }
                    boolean nameMatch = normalizedUserName.equals(normalizedResUserName);
                    
                    isMaster = phoneMatch && nameMatch;
                    
                    log.info("[AUTH] NIBS master 권한 확인 - phoneNumber: {} vs {}, userName: {} vs {}, isMaster: {}", 
                            normalizedPhoneNumber, normalizedAgentUsrPhone, 
                            normalizedUserName, normalizedResUserName, isMaster);
                }
            }
            
            result.put("isMaster", isMaster);
            
            return result;
            
        } catch (RestClientException e) {
            log.error("[AUTH] NIBS 계약 로그인 API 호출 중 오류 발생", e);
            throw new Exception("NIBS 계약 로그인 API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

}
