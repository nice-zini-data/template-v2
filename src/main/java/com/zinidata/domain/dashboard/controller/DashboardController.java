package com.zinidata.domain.dashboard.controller;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 대시보드 페이지 컨트롤러
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

     /**
     * 대시보드 페이지 (/dashboard)
     * 
     * @param model 뷰 모델
     * @return 대시보드 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/dashboard/dashboard")
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("currentPage", "dashboard");
        return "dashboard/dashboard";
    }

     /**
     * 매출 급등락 가맹점 페이지 (/dashboard/salesSurge)
     * 
     * @param model 뷰 모델
     * @return 매출 급등락 가맹점 페이지 뷰 이름
     */
     @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/dashboard/salesSurge")
     @GetMapping("/salesSurge")
     public String salesSurge(Model model) {
         model.addAttribute("pageTitle", "매출 급등락 가맹점");
         model.addAttribute("currentPage", "dashboard/salesSurge");
         return "dashboard/salesSurge";
     }

     /**
     * 거래 중단 알림 페이지 (/dashboard/tradeAlert)
     * 
     * @param model 뷰 모델
     * @return 거래 중단 알림 페이지 뷰 이름
     */
     @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/dashboard/tradeAlert")
     @GetMapping("/tradeAlert")
     public String tradeAlert(Model model) {
         model.addAttribute("pageTitle", "거래 중단 알림");
         model.addAttribute("currentPage", "dashboard/tradeAlert");
         return "dashboard/tradeAlert";
     }
     
      /**
     * 전체 가맹점 확인 페이지 (/dashboard/franchiseList)
     * 
     * @param model 뷰 모델
     * @return 전체 가맹점 확인 페이지 뷰 이름
     */
      @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/dashboard/franchiseList")
      @GetMapping("/franchiseList")
      public String franchiseList(Model model) {
          model.addAttribute("pageTitle", "전체 가맹점 확인");
          model.addAttribute("currentPage", "dashboard/franchiseList");
          return "dashboard/franchiseList";
      }

       /**
     * 매출 캘린더 페이지 (/dashboard/salesCalendar)
     * 
     * @param model 뷰 모델
     * @return 매출 캘린더 페이지 뷰 이름
     */
       @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/dashboard/salesCalendar")
       @GetMapping("/salesCalendar")
       public String salesCalendar(Model model) {
           model.addAttribute("pageTitle", "매출 캘린더");
           model.addAttribute("currentPage", "dashboard/salesCalendar");
           return "dashboard/salesCalendar";
       }

       /**
     * 서비스 변경 페이지 (/dashboard/changeService)
     * 
     * @param model 뷰 모델
     * @return 서비스 변경 페이지 뷰 이름
     */
       @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/dashboard/changeService")
       @GetMapping("/changeService")
       public String changeService(Model model) {
           model.addAttribute("pageTitle", "서비스 변경");
           model.addAttribute("currentPage", "dashboard/changeService");
           return "dashboard/changeService";
       }
}