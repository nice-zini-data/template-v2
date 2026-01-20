package com.zinidata.domain.home.controller;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 홈 도메인 페이지 컨트롤러
 * 
 * <p>메인 페이지, 약관 페이지 등 홈 도메인 관련 페이지를 제공합니다.</p>
 * <p>서비스 약관, 개인정보처리방침 등 정적 페이지를 포함합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    /**
     * 홈 페이지 (/, /home)
     * 
     * @param model 뷰 모델
     * @return 홈 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/home")
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        log.info("[HOME_CONTROLLER] 홈 페이지 접속");
        
        model.addAttribute("pageTitle", "홈");
        model.addAttribute("currentPage", "home");
        return "home";
    }

}