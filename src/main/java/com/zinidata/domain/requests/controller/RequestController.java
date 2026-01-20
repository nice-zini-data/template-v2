package com.zinidata.domain.requests.controller;

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
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    

    /**
     * 요청 등록 / 신규 설치 페이지 (/requests/install)
     * 
     * @param model 뷰 모델
     * @return 요청 등록 / 신규 설치 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/install")
    @GetMapping("/install")
    public String install(Model model) {
        model.addAttribute("pageTitle", "요청 등록");
        model.addAttribute("currentPage", "requests/install");
        return "requests/install";
    }

    /**
     * 요청 등록 / A/S 페이지 (/requests/as)
     * 
     * @param model 뷰 모델
     * @return 요청 등록 / A/S 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/as")
    @GetMapping("/as")
    public String as(Model model) {
        model.addAttribute("pageTitle", "요청 등록");
        model.addAttribute("currentPage", "requests/as");
        return "requests/as";
    }

    /**
     * 요청 내역 페이지 (/requests/request-history)
     * 
     * @param model 뷰 모델
     * @return 요청 내역 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/request-history")
    @GetMapping("/request-history")
    public String requestHistory(Model model) {
        model.addAttribute("pageTitle", "내 활동");
        model.addAttribute("currentPage", "requests/request-history");
        return "requests/request-history";
    }

    /**
     * 요청 내역 상세 페이지 (/requests/request-history-install-view)
     * 요청 내역 신규 설치 상세 페이지
     * @param model 뷰 모델
     * @return 요청 내역 신규 설치 상세 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/request-history-install-view")
    @GetMapping("/request-history-install-view")
    public String requestHistoryInstallView(Model model) {
        model.addAttribute("pageTitle", "요청 내역 상세보기");
        model.addAttribute("currentPage", "requests/request-history-install-view");
        return "requests/request-history-install-view";
    }

    /**
     * 요청 내역 상세 페이지 (/requests/request-history-as-view)   
     * 요청 내역 A/S 상세 페이지
     * @param model 뷰 모델
     * @return 요청 내역 A/S 상세 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/request-history-as-view")
    @GetMapping("/request-history-as-view")
    public String requestHistoryAsView(Model model) {
        model.addAttribute("pageTitle", "요청 내역 상세보기");
        model.addAttribute("currentPage", "requests/request-history-as-view");
        return "requests/request-history-as-view";
    }

    /**
     * 수행 내역 페이지 (/requests/work-history)
     * 
     * @param model 뷰 모델
     * @return 수행 내역 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/work-history")
    @GetMapping("/work-history")
    public String workHistory(Model model) {
        model.addAttribute("pageTitle", "내 활동");
        model.addAttribute("currentPage", "requests/work-history");
        return "requests/work-history";
    }

     /**
     * 수행 내역 신규 설치 상세 페이지 (/requests/work-history-install-view)
     * 
     * @param model 뷰 모델
     * @return 수행 내역 신규 설치 상세 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/work-history-install-view")
    @GetMapping("/work-history-install-view")
    public String workHistoryInstallView(Model model) {
        model.addAttribute("pageTitle", "내 활동 상세보기");
        model.addAttribute("currentPage", "requests/work-history-install-view");
        return "requests/work-history-install-view";
    }

     /**
     * 수행 내역 A/S 상세 페이지 (/requests/work-history-as-view)
     * 
     * @param model 뷰 모델
     * @return 수행 내역 A/S 상세 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/work-history-as-view")
    @GetMapping("/work-history-as-view")
    public String workHistoryAsView(Model model) {
        model.addAttribute("pageTitle", "수행 내역 상세보기");
        model.addAttribute("currentPage", "requests/work-history-as-view");
        return "requests/work-history-as-view";
    }


    /**
     * 주소 검색 페이지 (/requests/address-search)
     * 
     * @param model 뷰 모델
     * @return 주소 검색 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/address-search")
    @GetMapping("/address-search")
    public String addressSearch(Model model) {
        model.addAttribute("pageTitle", "주소 검색");
        model.addAttribute("currentPage", "requests/address-search");
        return "requests/address-search";
    }

    /**
     * 이미지 뷰어 페이지 (/requests/image-viewer)
     * 
     * @param model 뷰 모델
     * @return 이미지 뷰어 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/image-viewer")
    @GetMapping("/image-viewer")
    public String imageViewer(Model model) {
        model.addAttribute("pageTitle", "이미지 미리보기");
        model.addAttribute("currentPage", "requests/image-viewer");
        return "requests/image-viewer";
    }

    
     /**
     * 완료 증빙 페이지 (/requests/work-completion-proof)
     * 
     * @param model 뷰 모델
     * @return 완료 증빙 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/work-completion-proof")
    @GetMapping("/work-completion-proof")
    public String workCompletionProof(Model model) {
        model.addAttribute("pageTitle", "완료 증빙");
        model.addAttribute("currentPage", "requests/work-completion-proof");
        return "requests/work-completion-proof";
    }

     /**
     * 요청 탐색 페이지 (/requests/map)
     * 
     * @param model 뷰 모델
     * @return 완료 증빙 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/requests/map")
    @GetMapping("/map")
    public String map(Model model) {
        model.addAttribute("pageTitle", "요청 탐색");
        model.addAttribute("currentPage", "requests/map");
        return "requests/map";
    }
}