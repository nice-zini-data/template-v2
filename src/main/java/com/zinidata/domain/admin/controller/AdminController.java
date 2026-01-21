package com.zinidata.domain.admin.controller;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 페이지 컨트롤러
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

     /**
     * 관리자 페이지 (/admin)
     * 
     * @param model 뷰 모델
     * @return 관리자 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/admin/admin")
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("pageTitle", "관리자");
        model.addAttribute("currentPage", "admin");
        return "admin/admin";
    }

}