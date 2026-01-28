package com.zinidata.domain.report.controller;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 요청 도메인 페이지 컨트롤러
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    /**
     * 보고서 (/report/report)
     * 
     * @param model 뷰 모델
     * @return 보고서 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/report")
    @GetMapping("")
    public String report(Model model) {
        model.addAttribute("pageTitle", "보고서");
        model.addAttribute("currentPage", "report/report");
        return "report/report";
    }

}