package com.zinidata.domain.common.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 통합 인증 웹 컨트롤러
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 로그인 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/auth/login")
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        log.info("[AUTH-V1] 로그인 페이지 접속");
        
        // 로그인 페이지 접속 시 세션 정보 초기화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("[AUTH-V1] 세션 정보 초기화 완료");
        }
        
        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("pageDescription", "사용자 인증을 위한 로그인 페이지입니다.");
        model.addAttribute("currentPage", "auth");
        
        return "auth/login"; // Thymeleaf 템플릿 반환
    }
    
    /**
     * 아이디 찾기 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/auth/findId")
    @GetMapping("/findId")
    public String findIdPage(Model model) {
        log.info("[AUTH-V1] 아이디 찾기 페이지 접속");
        
        model.addAttribute("pageTitle", "아이디 찾기");
        model.addAttribute("pageDescription", "등록된 이메일로 아이디를 찾을 수 있습니다.");
        model.addAttribute("currentPage", "auth");
        
        return "auth/findId";
    }

    /**
     * 아이디 찾기 결과 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/auth/findIdResult")
    @GetMapping("/findId/result")
    public String findIdResult(Model model) {
        log.info("[AUTH-V1] 아이디 찾기 결과 페이지 접속");
        
        model.addAttribute("pageTitle", "아이디 찾기");
        model.addAttribute("pageDescription", "아이디 찾기 결과를 확인할 수 있습니다.");
        model.addAttribute("currentPage", "auth");
        
        return "auth/findIdResult";
    }
    
    /**
     * 비밀번호 찾기 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/auth/findPassword")
    @GetMapping("/findPassword")
    public String findPasswordPage(Model model) {
        log.info("[AUTH-V1] 비밀번호 찾기 페이지 접속");
        
        model.addAttribute("pageTitle", "비밀번호 찾기");
        model.addAttribute("pageDescription", "등록된 이메일로 비밀번호를 재설정할 수 있습니다.");
        model.addAttribute("currentPage", "auth");
        
        return "auth/findPassword";
    }


    /**
     * 비밀번호 변경 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/auth/findPasswordResult")
    @GetMapping("/findPasswordResult")
    public String findPasswordResult(Model model) {
        log.info("[AUTH-V1] 비밀번호 변경 페이지 접속");
        
        model.addAttribute("pageTitle", "비밀번호 변경");
        model.addAttribute("pageDescription", "새로운 비밀번호를 설정해주세요.");
        model.addAttribute("currentPage", "auth");
        
        return "auth/findPasswordResult";
    }

    /**
     * 회원가입 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/auth/signup")
    @GetMapping("/signup")
    public String signupPage(Model model) {
        log.info("[AUTH-V1] 회원가입 페이지 접속");
        
        model.addAttribute("pageTitle", "회원가입");
        model.addAttribute("pageDescription", "새로운 회원가입을 진행할 수 있습니다.");
        model.addAttribute("currentPage", "auth");
        
        return "auth/signup";
    }
} 